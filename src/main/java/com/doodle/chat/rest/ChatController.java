package com.doodle.chat.rest;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = "/api/v1/chat")
@CrossOrigin("*")
public class ChatController {
    
	@Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> useSimpleRest(@RequestBody  Map<String, String> message){
        if(message.containsKey("message")){
            if(message.containsKey("toId") && message.get("toId")!=null && !message.get("toId").equals("")){
                this.simpMessagingTemplate.convertAndSend("/socket-publisher/"+message.get("toId"),message);
                this.simpMessagingTemplate.convertAndSend("/socket-publisher/"+message.get("fromId"),message);
            }else{
                this.simpMessagingTemplate.convertAndSend("/socket-publisher",message);
            }
            return new ResponseEntity<>(message, new HttpHeaders(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @MessageMapping("/send/message")
    public Map<String, String> useSocketCommunication(String message){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> messageConverted = null;
        try {
            messageConverted = mapper.readValue(message, Map.class);
        } catch (IOException e) {
            messageConverted = null;
        }
        if(messageConverted!=null){
            if(messageConverted.containsKey("toId") && messageConverted.get("toId")!=null && !messageConverted.get("toId").equals("")){
                this.simpMessagingTemplate.convertAndSend("/socket-publisher/"+messageConverted.get("toId"),messageConverted);
                this.simpMessagingTemplate.convertAndSend("/socket-publisher/"+messageConverted.get("fromId"),message);
            }else{
                this.simpMessagingTemplate.convertAndSend("/socket-publisher",messageConverted);
            }
        }
        return messageConverted;
    }

}