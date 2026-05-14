package com.greedy.mokkoji.db.admin.repository;

import com.greedy.mokkoji.db.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
