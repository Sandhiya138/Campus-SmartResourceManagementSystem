package com.example.demo.service;
import org.springframework.stereotype.Service;

//Beginner version: only prints to the console.
//Later you can replace the println with real email (JavaMailSender) or SMS.
@Service
public class NotificationService {
 public void send(String username, String message) {
     System.out.println("[EMAIL/SMS to " + username + "] " + message);
 }
}
