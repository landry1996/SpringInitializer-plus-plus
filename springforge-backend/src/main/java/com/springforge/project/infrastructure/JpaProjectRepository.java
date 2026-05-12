package com.springforge.project.infrastructure;

import com.springforge.project.domain.Project;
import com.springforge.project.domain.ProjectRepository;
import com.springforge.project.domain.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProjectRepository extends JpaRepository<Project, UUID>, ProjectRepository {

    @Override
    default List<Project> findByOwnerId(UUID ownerId) {
        return findByOwnerIdAndStatusNot(ownerId, ProjectStatus.DELETED);
    }

    List<Project> findByOwnerIdAndStatusNot(UUID ownerId, ProjectStatus status);
}
