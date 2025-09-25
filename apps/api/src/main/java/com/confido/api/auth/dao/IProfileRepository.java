package com.confido.api.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.confido.api.auth.models.Profile;

@Repository
public interface IProfileRepository extends JpaRepository<Profile, Long> {}
