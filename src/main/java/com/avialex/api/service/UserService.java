package com.avialex.api.service;

import com.avialex.api.model.entity.User;
import com.avialex.api.model.entity.UserType;
import com.avialex.api.repository.ProcessRepository;
import com.avialex.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProcessRepository processRepository;

    @Autowired
    private UserService(UserRepository userRepository, ProcessRepository processRepository) {
        this.userRepository = userRepository;
        this.processRepository = processRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> listUsers(String name, String cpf, String email, UserType type) {
        return userRepository.findAll().stream()
                .filter(u -> (name == null || u.getName().equalsIgnoreCase(name)))
                .filter(u -> (cpf == null || u.getCpf().equalsIgnoreCase(cpf)))
                .filter(u -> (email == null || u.getEmail().equalsIgnoreCase(email)))
                .filter(u -> (type == null || u.getType() == type)).toList();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User user) {
        Optional<User> existing = findById(id);
        if (existing.isEmpty()) throw new java.util.NoSuchElementException("User not found");
        User toUpdate = existing.get();
        if (user.getName() != null) toUpdate.setName(user.getName());
        if (user.getAddress() != null) toUpdate.setAddress(user.getAddress());
        if (user.getEmail() != null) toUpdate.setEmail(user.getEmail());
        if (user.getPhone() != null) toUpdate.setPhone(user.getPhone());
        if (user.getPassword() != null) toUpdate.setPassword(user.getPassword());
        if (user.getCpf() != null) toUpdate.setCpf(user.getCpf());
        if (user.getRg() != null) toUpdate.setRg(user.getRg());
        if (user.getType() != null) toUpdate.setType(user.getType());
        return userRepository.save(toUpdate);
    }

    public void deleteUser(Long id) {
        Optional<User> user = findById(id);
        if (user.isEmpty()) throw new java.util.NoSuchElementException("User not found");

        // Verificar se o usuário tem processos associados
        if (processRepository.existsByClientId_Id(id)) {
            throw new IllegalStateException("Cannot delete user with associated processes. Please delete or reassign the user's processes first.");
        }

        userRepository.delete(user.get());
    }

    public List<User> searchUsers(String name, String address, String email, String phone, String cpf, String rg, UserType type) {
        return userRepository.findAll().stream()
                .filter(u -> name == null || u.getName().equalsIgnoreCase(name))
                .filter(u -> address == null || u.getAddress().equalsIgnoreCase(address))
                .filter(u -> email == null || u.getEmail().equalsIgnoreCase(email))
                .filter(u -> phone == null || u.getPhone().equalsIgnoreCase(phone))
                .filter(u -> cpf == null || u.getCpf().equalsIgnoreCase(cpf))
                .filter(u -> rg == null || u.getRg().equalsIgnoreCase(rg))
                .filter(u -> type == null || u.getType() == type)
                .toList();
    }
}