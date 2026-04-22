package com.notecurve.admin.client;

import com.notecurve.admin.dto.AdminUserDTO;
import com.notecurve.admin.dto.AdminPostDTO;
import com.notecurve.admin.dto.AdminCommentDTO;
import com.notecurve.admin.dto.AdminMessageBoardDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 동기화(sync) 전용 - GET 조회만 사용
@FeignClient(name = "auth-service", url = "${main.server.url}")
public interface AuthServiceClient {

    @GetMapping("/api/users/internal/all")
    List<AdminUserDTO> getAllUsers(@RequestHeader("Cookie") String cookie);

    @GetMapping("/api/posts/internal/all")
    List<AdminPostDTO> getAllPosts(@RequestHeader("Cookie") String cookie);

    @GetMapping("/api/message-boards/internal/all")
    List<AdminMessageBoardDTO> getAllMessageBoards(@RequestHeader("Cookie") String cookie);

    @GetMapping("/api/comments/internal/all")
    List<AdminCommentDTO> getAllComments(@RequestHeader("Cookie") String cookie);
}
