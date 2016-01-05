/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.repository;

import com.largecode.interview.rustem.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
public interface UsersRepository extends JpaRepository<User, Long>{
    Page findAllByOrderByEmailAsc(Pageable pageable);
    Optional<User> findOneByEmail(String email);
    
}
