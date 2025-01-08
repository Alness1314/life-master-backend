package com.alness.lifemaster.app.service.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.app.service.AppConfigService;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.common.enums.AllowedProfiles;
import com.alness.lifemaster.profiles.dto.request.ProfileRequest;
import com.alness.lifemaster.profiles.dto.response.ProfileResponse;
import com.alness.lifemaster.profiles.service.ProfileService;
import com.alness.lifemaster.users.dto.request.UserRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {
    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseDto createDefaultValues() {
        boolean resultAdmin = createProfile(AllowedProfiles.ADMIN.getName());
        boolean resultEmployee = createProfile(AllowedProfiles.EMPLOYEE.getName());
        boolean resultUser = createProfile(AllowedProfiles.USER.getName());
        String profile = AllowedProfiles.ADMIN.getName();
        boolean resultDefaultUser = createUser(
                new UserRequest("administrator@gmail.com", "admin025#", Collections.singletonList(profile)));

        if (resultAdmin && resultEmployee && resultUser && resultDefaultUser) {
            return new ResponseDto("All profiles were created successfully.", HttpStatus.OK, true);
        } else {
            return new ResponseDto("Failed to create profiles, some profiles were not created.", HttpStatus.BAD_REQUEST,
                    false);
        }
    }

    private boolean saveProfile(String profileName) {
        ProfileResponse profile = profileService.save(new ProfileRequest(profileName));
        return profile != null;
    }

    private boolean findProfile(String profileName) {
        ProfileResponse profile = profileService.findByName(profileName);
        return profile != null;
    }

    private Boolean createProfile(String profileName) {
        if (findProfile(profileName)) {
            log.info(String.format("The profile with the name: [%s] already exists.", profileName));
            return false;
        } else {
            log.info(String.format("The profile with the name: [%s] created successfully.", profileName));
            return saveProfile(profileName);
        }
    }

    private boolean saveUser(UserRequest userDefautl) {
        UserResponse user = userService.save(userDefautl);
        return user != null;
    }

    private boolean findUser(String username) {
        UserResponse user = userService.findByUsername(username);
        return user != null;
    }

    private Boolean createUser(UserRequest userDefault) {
        if (findUser(userDefault.getUsername())) {
            log.info(String.format("The user with the username: [%s] already exists.", userDefault.getUsername()));
            return false;
        } else {
            log.info(
                    String.format("The user with the username: [%s] created successfully.", userDefault.getUsername()));
            return saveUser(userDefault);
        }
    }
}
