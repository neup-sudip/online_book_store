package com.example.first.user;

import com.example.first.utils.JwtConfig;
import com.example.first.utils.ResponseData;
import com.example.first.utils.ResponseWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseWrapper getAllUsers() {
        List<User> users = userService.getUsers();
        List<UserResponseDto> resUser = new ArrayList<>();
        for (User user : users) {
            resUser.add(new UserResponseDto(user.getUsername(), user.getEmail(), User.ROLE.valueOf(user.getRole())));
        }
        ResponseData res = new ResponseData(resUser, "All users fetched", true);
        return new ResponseWrapper(res, 200);
    }

    @PostMapping("/register")
    public ResponseWrapper createUser(@Valid @RequestBody NewUserReqDto userReqDto) {

        User newUser = new User(userReqDto.getUsername(), userReqDto.getPassword(), userReqDto.getEmail(), userReqDto.getRole());

        User user = userService.addNewUser(newUser);
        User.ROLE role = User.ROLE.valueOf(user.getRole());

        UserResponseDto resUser = new UserResponseDto(user.getUsername(), user.getEmail(), role);
        return new ResponseWrapper(new ResponseData(resUser, "User created successfully !", true), 200);
    }

    @PostMapping("/login")
    public ResponseWrapper loginUser(@RequestBody User user, HttpServletResponse response, HttpServletRequest request) {
        User returnUser = userService.login(user.getUsername(), user.getPassword());
        if (returnUser != null) {
            JwtConfig jwtConfig = new JwtConfig();
            returnUser.setPassword("");
            String token = jwtConfig.generateToken(returnUser);

            final Cookie cookie = new Cookie("auth", token);
            cookie.setSecure(false);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(100);
            cookie.setPath("/");
            response.addCookie(cookie);

            return new ResponseWrapper(new ResponseData(token, "user success", true), 200);
        } else {
            return new ResponseWrapper(new ResponseData("token", "user not found", false), 400);
        }
    }

    @GetMapping("/{id}")
    public ResponseWrapper getUserById(@PathVariable long id) {
        User user = userService.getUserById(id);

        if (user == null) {
            return new ResponseWrapper(new ResponseData(null, "User not found !", false), 404);
        } else {
            User.ROLE role = User.ROLE.valueOf(user.getRole());
            UserResponseDto resUser = new UserResponseDto(user.getUsername(), user.getEmail(), role);
            return new ResponseWrapper(new ResponseData(resUser, "User fetched successfully", true), 200);
        }
    }

    @PutMapping("/{id}")
    public ResponseWrapper updateUser(@PathVariable long id, @RequestBody User user) {
        return userService.updateUser(user, id);
    }


}
