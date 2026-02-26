package com.dasi.infrastructure.repository;

import com.dasi.domain.workspace.model.enumeration.PlazaActionType;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.infrastructure.persistent.dao.IAiPlazaCommentDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaFavorDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaLikeDao;
import com.dasi.infrastructure.persistent.po.AiPlaza;
import com.dasi.infrastructure.persistent.po.AiPlazaComment;
import com.dasi.infrastructure.persistent.po.AiPlazaFavor;
import com.dasi.infrastructure.persistent.po.AiPlazaLike;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class WorkspaceRepository implements IWorkspaceRepository {

    @Resource
    private UserContext userContext;

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Resource
    private IAiPlazaLikeDao aiPlazaLikeDao;

    @Resource
    private IAiPlazaFavorDao aiPlazaFavorDao;

    @Resource
    private IAiPlazaCommentDao aiPlazaCommentDao;

    @Resource
    private IRandomUtil randomUtil;

    @Override
    public PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto) {

        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AiPlaza> aiPlazaList = aiPlazaDao.page(
                dto.getKeyword(),
                dto.getSortBy(),
                dto.getSortOrder(),
                offset,
                pageSize
        );

        Integer total = aiPlazaDao.count(dto.getKeyword());
        if (total == null) {
            total = 0;
        }

        List<PlazaVO> plazaVOList = List.of();
        if (aiPlazaList != null && !aiPlazaList.isEmpty()) {
            Long userId = userContext.getUserId();

            List<String> plazaIdList = aiPlazaList.stream().map(AiPlaza::getPlazaId).toList();
            Set<String> likedSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.LIKE);
            Set<String> favoredSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.FAVOR);
            Set<String> commentedSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.COMMENT);

            plazaVOList = aiPlazaList.stream().map(aiPlaza -> PlazaVO.builder()
                    .plazaId(aiPlaza.getPlazaId())
                    .templateId(aiPlaza.getTemplateId())
                    .agentId(aiPlaza.getAgentId())
                    .agentType(aiPlaza.getAgentType())
                    .userName(aiPlaza.getUserName())
                    .plazaTitle(aiPlaza.getPlazaTitle())
                    .plazaDesc(aiPlaza.getPlazaDesc())
                    .likeCount(aiPlaza.getLikeCount())
                    .favorCount(aiPlaza.getFavorCount())
                    .commentCount(aiPlaza.getCommentCount())
                    .liked(likedSet.contains(aiPlaza.getPlazaId()))
                    .favored(favoredSet.contains(aiPlaza.getPlazaId()))
                    .commented(commentedSet.contains(aiPlaza.getPlazaId()))
                    .createTime(aiPlaza.getCreateTime())
                    .build()).toList();
        }

        int pageSum = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        return PageResult.<PlazaVO>builder()
                .list(plazaVOList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto) {
        Long userId = userContext.getUserId();
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        String plazaId = dto.getPlazaId();

        List<AiPlazaComment> aiPlazaCommentList = aiPlazaCommentDao.listByPlazaId(plazaId, offset, pageSize);
        List<CommentVO> commentVOList = List.of();
        if (aiPlazaCommentList != null && !aiPlazaCommentList.isEmpty()) {
            commentVOList = aiPlazaCommentList.stream().map(aiPlazaComment -> {
                Long commentUserId = aiPlazaComment.getUserId();
                return CommentVO.builder()
                        .commentId(aiPlazaComment.getCommentId())
                        .plazaId(aiPlazaComment.getPlazaId())
                        .userId(commentUserId)
                        .userName(aiPlazaComment.getUserName())
                        .commentContent(aiPlazaComment.getCommentContent())
                        .createTime(aiPlazaComment.getCreateTime())
                        .mine(userId != null && userId.equals(commentUserId))
                        .build();
            }).toList();
        }

        Integer total = aiPlazaCommentDao.countByPlazaId(plazaId);
        if (total == null) {
            total = 0;
        }
        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<CommentVO>builder()
                .list(commentVOList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public void plazaLike(String plazaId, boolean liked) {
        Long userId = userContext.getUserId();
        if (liked) {
            Integer affected = aiPlazaLikeDao.insert(AiPlazaLike.builder().plazaId(plazaId).userId(userId).build());
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseLikeCount(plazaId, 1);
            }
        } else {
            Integer affected = aiPlazaLikeDao.delete(plazaId, userId);
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseLikeCount(plazaId, -1);
            }
        }

    }

    @Override
    public void plazaFavor(String plazaId, boolean favored) {
        Long userId = userContext.getUserId();
        if (favored) {
            Integer affected = aiPlazaFavorDao.insert(AiPlazaFavor.builder().plazaId(plazaId).userId(userId).build());
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseFavorCount(plazaId, 1);
            }
        } else {
            Integer affected = aiPlazaFavorDao.delete(plazaId, userId);
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseFavorCount(plazaId, -1);
            }
        }

    }

    @Override
    public void plazaComment(PlazaCommentDTO dto) {
        String plazaId = dto.getPlazaId();
        Long userId = userContext.getUserId();
        aiPlazaCommentDao.insert(AiPlazaComment.builder()
                .commentId(randomUtil.userRandom())
                .plazaId(plazaId)
                .userId(userId)
                .userName(userContext.getUserName())
                .commentContent(dto.getCommentContent())
                .build());
        aiPlazaDao.increaseCommentCount(plazaId, 1);
    }

    @Override
    public void plazaDiscomment(String plazaId, String commentId) {
        Long userId = userContext.getUserId();
        Integer affected = aiPlazaCommentDao.delete(commentId, userId);
        if (affected != null && affected > 0) {
            aiPlazaDao.increaseCommentCount(plazaId, -1);
        }
    }

    private Set<String> queryUserPlazaSet(Long userId, List<String> plazaIdList, PlazaActionType actionType) {
        if (userId == null || plazaIdList == null || plazaIdList.isEmpty()) {
            return new HashSet<>();
        }

        List<String> dataList;
        switch (actionType) {
            case LIKE -> dataList = aiPlazaLikeDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            case FAVOR -> dataList = aiPlazaFavorDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            case COMMENT -> dataList = aiPlazaCommentDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            default -> dataList = List.of();
        }
        if (dataList == null || dataList.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(dataList);
    }

}
