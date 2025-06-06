package com.ssafy.spico.domain.project.service

import com.ssafy.spico.domain.practice.dto.PracticeListResponseDto
import com.ssafy.spico.domain.practice.dto.PracticeResponseDto
import com.ssafy.spico.domain.practice.dto.toResponse
import com.ssafy.spico.domain.practice.service.DeletePracticeService
import com.ssafy.spico.domain.practice.entity.PracticeType
import com.ssafy.spico.domain.practice.model.Practice
import com.ssafy.spico.domain.practice.model.toModel
import com.ssafy.spico.domain.practice.repository.CoachingReportsRepository
import com.ssafy.spico.domain.practice.repository.FinalReportsRepository
import com.ssafy.spico.domain.project.dto.ProjectViewType
import com.ssafy.spico.domain.project.dto.UpdateProjectRequestDto
import com.ssafy.spico.domain.project.dto.toCommand
import com.ssafy.spico.domain.project.exception.ProjectError
import com.ssafy.spico.domain.project.exception.ProjectException
import com.ssafy.spico.domain.project.model.Project
import com.ssafy.spico.domain.project.model.toEntity
import com.ssafy.spico.domain.project.model.toModel
import com.ssafy.spico.domain.project.repository.ProjectRepository
import com.ssafy.spico.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val deletePracticeService: DeletePracticeService,
    private val coachingReportsRepository: CoachingReportsRepository,
    private val finalReportsRepository: FinalReportsRepository
): ProjectService {
    override fun getProjects(userId: Int, cursor: Int?, size: Int, type: ProjectViewType): List<Project> {

        require(size >= 1) { throw ProjectException(ProjectError.INVALID_PAGE_SIZE) }

        val entities = projectRepository.findProjectsWithPaging(userId, cursor, size, type)
        return entities.map { it.toModel() }
    }

    @Transactional
    override fun createProject(project: Project): Int {
        val userEntity = userRepository.findById(project.userId)
            .orElseThrow { ProjectException(ProjectError.USER_NOT_FOUND) }
        val projectEntity = project.toEntity(userEntity)
        val saved = try {
            projectRepository.save(projectEntity)
        } catch (e: Exception) {
            throw ProjectException(ProjectError.PERSISTENCE_ERROR)
        }
        return saved.projectId
    }

    @Transactional
    override fun updateProject(projectId: Int, request: UpdateProjectRequestDto) {
        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { ProjectException(ProjectError.PROJECT_NOT_FOUND) }
        val command = request.toCommand()
        try {
            projectEntity.updateProject(command)
        } catch (e: Exception) {
            throw ProjectException(ProjectError.INVALID_UPDATE_REQUEST)
        }
    }

    @Transactional
    override fun deleteProject(userId: Int, projectId: Int) {
        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { ProjectException(ProjectError.PROJECT_NOT_FOUND) }
        try {
            val practiceIds: List<Int> = projectRepository.findPracticeIdsByProjectId(userId, projectId)
            practiceIds.forEach { practiceId ->
                deletePracticeService.deletePractice(practiceId)
            }
            projectRepository.delete(projectEntity)
        } catch (e: Exception) {
            throw ProjectException(ProjectError.DELETE_FAILED)
        }
    }

    override fun getProjectDetail(projectId: Int): Project {
        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { ProjectException(ProjectError.PROJECT_NOT_FOUND) }
        return projectEntity.toModel()
    }

    override fun getPractices(
        userId: Int,
        projectId: Int,
        practiceFilter: PracticeType?,
        cursor: Int?,
        size: Int
    ): List<PracticeResponseDto> {
        require(size >= 1) { throw ProjectException(ProjectError.INVALID_PAGE_SIZE) }

        val practices = projectRepository.findPracticesByProjectIdWithPaging(userId, projectId, practiceFilter, cursor, size).map { it.toModel() }
        return practices.map { practice ->
            practice.toResponse(coachingReportsRepository, finalReportsRepository)
        }
    }
}