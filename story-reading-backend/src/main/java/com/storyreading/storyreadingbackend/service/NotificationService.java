package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.NotificationResponse;
import com.storyreading.storyreadingbackend.entity.Notification;
import com.storyreading.storyreadingbackend.entity.User;
import com.storyreading.storyreadingbackend.repository.NotificationRepository;
import com.storyreading.storyreadingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<NotificationResponse> getMyNotifications(Authentication authentication, Pageable pageable) {
        User user = getCurrentUser(authentication);
        return notificationRepository.findByReceiver_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(this::toResponse);
    }

    public long countUnread(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationRepository.countByReceiver_UserIdAndIsReadFalse(user.getUserId());
    }

    public void markAsRead(Long notificationId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));

        if (!noti.getReceiver().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với thông báo này");
        }

        noti.setIsRead(true);
        notificationRepository.save(noti);
    }

    public void markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        Pageable all = Pageable.unpaged();
        notificationRepository.findByReceiver_UserIdOrderByCreatedAtDesc(user.getUserId(), all)
                .forEach(n -> {
                    if (!Boolean.TRUE.equals(n.getIsRead())) {
                        n.setIsRead(true);
                        notificationRepository.save(n);
                    }
                });
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getTitle(),
                n.getContent(),
                n.getType().name(),
                n.getIsRead(),
                n.getStory() != null ? n.getStory().getStoryId() : null,
                n.getCreatedAt()
        );
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));
    }
}
