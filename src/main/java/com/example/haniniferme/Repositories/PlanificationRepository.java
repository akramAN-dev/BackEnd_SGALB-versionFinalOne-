package com.example.haniniferme.Repositories;

import com.example.haniniferme.Entities.Archivage;
import com.example.haniniferme.Entities.Planification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanificationRepository extends JpaRepository<Planification,Long> {

}
