package com.nanhai.competition.websocket;

import com.nanhai.competition.dto.SubmissionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 比赛WebSocket处理器
 * 用于实时推送比赛数据更新
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CompetitionWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端发送的消息
     */
    @MessageMapping("/competition/submit")
    @SendTo("/topic/submissions")
    public SubmissionDTO handleSubmission(SubmissionDTO submission) {
        log.info("Received submission from user: {}", submission.getUserId());
        return submission;
    }

    /**
     * 广播提交记录更新
     */
    public void broadcastSubmission(SubmissionDTO submission) {
        messagingTemplate.convertAndSend("/topic/submissions", submission);
    }

    /**
     * 广播统计数据更新
     */
    public void broadcastStats(Object stats) {
        messagingTemplate.convertAndSend("/topic/stats", stats);
    }

    /**
     * 广播比赛状态更新
     */
    public void broadcastStatus(String status) {
        messagingTemplate.convertAndSend("/topic/status", status);
    }

    /**
     * 广播排行榜更新
     */
    public void broadcastRankings(Object rankings) {
        messagingTemplate.convertAndSend("/topic/rankings", rankings);
    }
}

