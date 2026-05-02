package com.sarthi.service;

import com.sarthi.dto.FeedbackDTO;
import com.sarthi.entity.FeedbackMaster;
import com.sarthi.entity.FeedbackReply;
import com.sarthi.repository.FeedbackMasterRepository;
import com.sarthi.repository.FeedbackReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMasterRepository masterRepository;

    @Autowired
    private FeedbackReplyRepository replyRepository;

    @Transactional
    public FeedbackDTO submitFeedback(FeedbackDTO dto) {
        FeedbackMaster master = new FeedbackMaster();
        master.setUserId(dto.getUserId());
        master.setUserCode(dto.getUserCode());
        master.setUserName(dto.getUserName());
        master.setProductType(dto.getProductType());
        master.setRoleName(dto.getRoleName());
        master.setSubject(dto.getSubject());
        master.setMessage(dto.getMessage());
        master.setPriority(dto.getPriority());
        master.setStatus("Pending");

        FeedbackMaster saved = masterRepository.save(master);
        return convertToDTO(saved);
    }

    @Transactional
    public FeedbackDTO replyToFeedback(Integer feedbackId, FeedbackDTO.FeedbackReplyDTO replyDto) {
        FeedbackMaster master = masterRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        FeedbackReply reply = new FeedbackReply();
        reply.setFeedback(master);
        reply.setUserId(replyDto.getUserId());
        reply.setUserCode(replyDto.getUserCode());
        reply.setUserName(replyDto.getUserName());
        reply.setRoleName(replyDto.getRoleName());
        reply.setProductType(replyDto.getProductType());
        reply.setReplyMessage(replyDto.getReplyMessage());

        replyRepository.save(reply);
        
        master.setStatus("Replied");
        masterRepository.save(master);

        return convertToDTO(master);
    }

    public List<FeedbackDTO> getFeedbackForUser(String userId) {
        return masterRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<FeedbackDTO> getAllFeedback() {
        return masterRepository.findAllByOrderByCreatedDateDesc()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private FeedbackDTO convertToDTO(FeedbackMaster master) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setFeedbackId(master.getFeedbackId());
        dto.setUserId(master.getUserId());
        dto.setUserCode(master.getUserCode());
        dto.setUserName(master.getUserName());
        dto.setProductType(master.getProductType());
        dto.setRoleName(master.getRoleName());
        dto.setSubject(master.getSubject());
        dto.setMessage(master.getMessage());
        dto.setPriority(master.getPriority());
        dto.setStatus(master.getStatus());
        dto.setCreatedDate(master.getCreatedDate());

        if (master.getReplies() != null) {
            dto.setReplies(master.getReplies().stream().map(r -> {
                FeedbackDTO.FeedbackReplyDTO rdto = new FeedbackDTO.FeedbackReplyDTO();
                rdto.setReplyId(r.getReplyId());
                rdto.setUserId(r.getUserId());
                rdto.setUserCode(r.getUserCode());
                rdto.setUserName(r.getUserName());
                rdto.setRoleName(r.getRoleName());
                rdto.setProductType(r.getProductType());
                rdto.setReplyMessage(r.getReplyMessage());
                rdto.setCreatedDate(r.getCreatedDate());
                return rdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
