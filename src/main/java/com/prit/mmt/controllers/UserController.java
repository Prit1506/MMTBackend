package com.prit.mmt.controllers;

import com.prit.mmt.models.Users;
import com.prit.mmt.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserServices userServices;

    @PostMapping("/login")
    public Users login(@RequestParam String email,@RequestParam String password){
        return userServices.login(email,password);
    }
    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody Users user){
        return ResponseEntity.ok(userServices.signup(user));
    }
    @GetMapping("/email")
    public ResponseEntity<Users> getuserbyemail(@RequestParam String email){
        Users user = userServices.getUserByEmail(email);
        if(user != null){
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/edit")
    public Users editprofile(@RequestParam String id ,@RequestBody Users updatedUser){
        return userServices.editprofile(id,updatedUser);
    }
}