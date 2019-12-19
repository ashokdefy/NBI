package com.nbirl.bank.service;

import com.nbirl.bank.model.Role;

public interface RoleService {

    Role findByRoleName(String roleName);
}
