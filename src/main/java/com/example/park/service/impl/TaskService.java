package com.example.park.service.impl;

import com.example.park.util.EmailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Async
    public void sendEmailCode(String email,String emailCode) throws Exception{
        EmailSender.sendEmail(""+emailCode.toString(),email);
    }
}
