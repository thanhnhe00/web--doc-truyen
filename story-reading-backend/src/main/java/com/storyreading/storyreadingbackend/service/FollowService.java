package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    public void follow(Long storyId, Authentication authentication) {
        User user = getUser(authentication);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));

        if (followRepository.existsByUser_UserIdAndStory_StoryId(user.getUserId(), story.getStoryId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã theo dõi truyện này rồi");
        }

        Follow follow = new Follow();
        follow.setUser(user);
        follow.setStory(story);
        followRepository.save(follow);
    }

    public void unfollow(Long storyId, Authentication authentication) {
        User user = getUser(authentication);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));
        followRepository.deleteByUser_UserIdAndStory_StoryId(user.getUserId(), story.getStoryId());
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));
    }
}