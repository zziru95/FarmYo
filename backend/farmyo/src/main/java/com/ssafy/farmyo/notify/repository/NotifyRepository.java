package com.ssafy.farmyo.notify.repository;

import com.ssafy.farmyo.entity.Notify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifyRepository extends JpaRepository<Notify, Integer> {
}
