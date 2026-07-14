package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.CommentRequest;
import com.storyreading.storyreadingbackend.dto.CommentResponse;
import com.storyreading.storyreadingbackend.dto.CommentUpdateRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.UserRole;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final SensitiveWordService sensitiveWordService;

    /**
     * FR07 - Tạo mới bình luận hoặc phản hồi.
     * Kiểm tra spam: nếu bình luận trước đó cách đây ít hơn 15 giây thì từ chối.
     */
    public Comment create(Long chapterId, CommentRequest request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        // FR07 - Kiểm soát spam: bình luận cách nhau tối thiểu 15 giây
        commentRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                .ifPresent(lastComment -> {
                    long secondsSinceLast = Duration.between(lastComment.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    if (secondsSinceLast < 15) {
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                                "Bạn bình luận quá nhanh. Vui lòng đợi " + (15 - secondsSinceLast) + " giây nữa.");
                    }
                });

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));

        // FR07 - Kiểm tra từ nhạy cảm và URL
        if (sensitiveWordService.containsUrl(request.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bình luận không được chứa liên kết URL");
        }
        String filteredContent = sensitiveWordService.filter(request.getContent());

        Comment comment = new Comment();
        comment.setChapter(chapter);
        comment.setUser(user);
        comment.setContent(filteredContent);
        comment.setIsHidden(false);

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận cha"));
            comment.setParent(parent);
        }

        return commentRepository.save(comment);
    }

    /**
     * FR07 - Lấy danh sách bình luận gốc (không phải phản hồi) cho 1 chapter.
     * Chỉ hiển thị bình luận chưa bị ẩn, kèm danh sách phản hồi.
     */
    public Page<CommentResponse> getByChapter(Long chapterId, Pageable pageable) {
        return commentRepository.findByChapter_ChapterIdAndParentIsNullAndIsHiddenFalse(chapterId, pageable)
                .map(this::toResponseWithReplies);
    }

    /**
     * FR07 - Lấy tất cả bình luận (kể cả bị ẩn) cho admin.
     */
    public Page<CommentResponse> getByChapterAdmin(Long chapterId, Pageable pageable) {
        return commentRepository.findByChapter_ChapterIdAndParentIsNull(chapterId, pageable)
                .map(this::toResponseWithRepliesAdmin);
    }

    /**
     * FR07 - Lấy danh sách phản hồi cho 1 bình luận.
     */
    public List<CommentResponse> getReplies(Long commentId) {
        return commentRepository.findByParent_CommentIdAndIsHiddenFalseOrderByCreatedAtAsc(commentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * FR07 - Chỉnh sửa bình luận. Chỉ tác giả mới được sửa.
     */
    public Comment update(Long commentId, CommentUpdateRequest request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));

        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(request.getContent());
        return commentRepository.save(comment);
    }

    /**
     * FR07 - Xóa bình luận. Tác giả hoặc Admin được xóa.
     */
    public void delete(Long commentId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));

        boolean isOwner = comment.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa bình luận này");
        }

        // Xóa tất cả phản hồi con trước
        List<Comment> replies = commentRepository.findByParent_CommentIdOrderByCreatedAtAsc(commentId);
        commentRepository.deleteAll(replies);
        commentRepository.delete(comment);
    }

    /**
     * FR07 - Ẩn bình luận (Admin only).
     */
    public Comment hide(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));
        comment.setIsHidden(true);
        return commentRepository.save(comment);
    }

    /**
     * FR07 - Hiện lại bình luận (Admin only).
     */
    public Comment unhide(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"));
        comment.setIsHidden(false);
        return commentRepository.save(comment);
    }

    /**
     * FR12 - Lấy danh sách bình luận bị ẩn cho admin.
     */
    public Page<CommentResponse> getHiddenComments(Pageable pageable) {
        return commentRepository.findByIsHiddenTrue(pageable)
                .map(this::toResponse);
    }

    /**
     * Đếm số bình luận hiển thị cho 1 chapter.
     */
    public long countVisible(Long chapterId) {
        return commentRepository.countByChapter_ChapterIdAndIsHiddenFalse(chapterId);
    }

    private CommentResponse toResponseWithReplies(Comment c) {
        List<CommentResponse> replies = getReplies(c.getCommentId());
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getUser().getUsername(),
                c.getUser().getAvatarUrl(),
                null,
                replies,
                c.getCreatedAt(),
                c.getCreatedAt() != null ? c.getCreatedAt() : c.getCreatedAt(),
                false
        );
    }

    private CommentResponse toResponseWithRepliesAdmin(Comment c) {
        List<CommentResponse> replies = commentRepository.findByParent_CommentIdOrderByCreatedAtAsc(c.getCommentId())
                .stream().map(this::toResponseAdmin).collect(Collectors.toList());
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getUser().getUsername(),
                c.getUser().getAvatarUrl(),
                null,
                replies,
                c.getCreatedAt(),
                c.getCreatedAt() != null ? c.getCreatedAt() : c.getCreatedAt(),
                c.getIsHidden()
        );
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getUser().getUsername(),
                c.getUser().getAvatarUrl(),
                c.getParent() != null ? c.getParent().getCommentId() : null,
                Collections.emptyList(),
                c.getCreatedAt(),
                c.getCreatedAt() != null ? c.getCreatedAt() : c.getCreatedAt(),
                false
        );
    }

    private CommentResponse toResponseAdmin(Comment c) {
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getUser().getUsername(),
                c.getUser().getAvatarUrl(),
                c.getParent() != null ? c.getParent().getCommentId() : null,
                Collections.emptyList(),
                c.getCreatedAt(),
                c.getCreatedAt() != null ? c.getCreatedAt() : c.getCreatedAt(),
                c.getIsHidden()
        );
    }
}
