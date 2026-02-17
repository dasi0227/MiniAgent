package com.dasi.infrastructure.repository;

import com.dasi.domain.plaza.repository.IPlazaRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaListRequest;
import com.dasi.types.dto.request.plaza.PlazaPublishRequest;
import com.dasi.types.dto.response.plaza.PlazaCommentResponse;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AdminException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PlazaRepository implements IPlazaRepository {

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Resource
    private IAiPlazaLikeDao aiPlazaLikeDao;

    @Resource
    private IAiPlazaFavorDao aiPlazaFavorDao;

    @Resource
    private IAiPlazaCommentDao aiPlazaCommentDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IUserDao userDao;

    @Override
    public PageResult<PlazaItemResponse> list(Long userId, PlazaListRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AiPlaza> aiPlazaList = aiPlazaDao.list(request.getTitleKeyword(), offset, pageSize);
        int total = aiPlazaDao.count(request.getTitleKeyword());

        List<PlazaItemResponse> responseList = new ArrayList<>();
        for (AiPlaza aiPlaza : aiPlazaList) {
            boolean liked = aiPlazaLikeDao.queryByPlazaIdAndUserId(aiPlaza.getPlazaId(), userId) != null;
            boolean favored = aiPlazaFavorDao.queryByPlazaIdAndUserId(aiPlaza.getPlazaId(), userId) != null;
            responseList.add(PlazaItemResponse.builder()
                    .plazaId(aiPlaza.getPlazaId())
                    .agentId(aiPlaza.getAgentId())
                    .plazaTitle(aiPlaza.getPlazaTitle())
                    .plazaDesc(aiPlaza.getPlazaDesc())
                    .likeCount(aiPlaza.getLikeCount())
                    .favorCount(aiPlaza.getFavorCount())
                    .commentCount(aiPlaza.getCommentCount())
                    .liked(liked)
                    .favored(favored)
                    .createTime(aiPlaza.getCreateTime())
                    .build());
        }

        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<PlazaItemResponse>builder()
                .list(responseList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public PlazaDetailResponse detail(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        boolean liked = aiPlazaLikeDao.queryByPlazaIdAndUserId(plazaId, userId) != null;
        boolean favored = aiPlazaFavorDao.queryByPlazaIdAndUserId(plazaId, userId) != null;
        PlazaItemResponse plazaItem = PlazaItemResponse.builder()
                .plazaId(aiPlaza.getPlazaId())
                .agentId(aiPlaza.getAgentId())
                .plazaTitle(aiPlaza.getPlazaTitle())
                .plazaDesc(aiPlaza.getPlazaDesc())
                .likeCount(aiPlaza.getLikeCount())
                .favorCount(aiPlaza.getFavorCount())
                .commentCount(aiPlaza.getCommentCount())
                .liked(liked)
                .favored(favored)
                .createTime(aiPlaza.getCreateTime())
                .build();

        List<AiPlazaComment> aiPlazaCommentList = aiPlazaCommentDao.listByPlazaId(plazaId, 0, 30);
        List<PlazaCommentResponse> commentList = new ArrayList<>();
        for (AiPlazaComment aiPlazaComment : aiPlazaCommentList) {
            User user = userDao.queryById(aiPlazaComment.getUserId());
            commentList.add(PlazaCommentResponse.builder()
                    .commentId(aiPlazaComment.getCommentId())
                    .plazaId(aiPlazaComment.getPlazaId())
                    .userId(aiPlazaComment.getUserId())
                    .username(user == null ? "未知用户" : user.getUsername())
                    .commentContent(aiPlazaComment.getCommentContent())
                    .createTime(aiPlazaComment.getCreateTime())
                    .build());
        }

        return PlazaDetailResponse.builder()
                .plazaItem(plazaItem)
                .commentList(commentList)
                .build();
    }

    @Override
    public void publish(Long userId, PlazaPublishRequest request) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdByOwner(request.getAgentId(), userId);
        if (aiAgent == null) {
            throw new AdminException("仅可发布自己创建的 Agent");
        }

        AiPlaza exists = aiPlazaDao.queryByAgentIdAndUserId(request.getAgentId(), userId);
        if (exists != null) {
            throw new AdminException("该 Agent 已发布");
        }

        aiPlazaDao.insert(AiPlaza.builder()
                .plazaId(UUID.randomUUID().toString().replace("-", ""))
                .agentId(request.getAgentId())
                .userId(userId)
                .plazaTitle(request.getPlazaTitle())
                .plazaDesc(StringUtils.hasText(request.getPlazaDesc()) ? request.getPlazaDesc() : "暂无")
                .plazaStatus(1)
                .likeCount(0)
                .favorCount(0)
                .commentCount(0)
                .build());
    }

    @Override
    public void like(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        AiPlazaLike exists = aiPlazaLikeDao.queryByPlazaIdAndUserId(plazaId, userId);
        if (exists == null) {
            aiPlazaLikeDao.insert(AiPlazaLike.builder().plazaId(plazaId).userId(userId).build());
            aiPlazaDao.increaseLikeCount(plazaId, 1);
            return;
        }

        aiPlazaLikeDao.deleteByPlazaIdAndUserId(plazaId, userId);
        aiPlazaDao.increaseLikeCount(plazaId, -1);
    }

    @Override
    public void favor(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        AiPlazaFavor exists = aiPlazaFavorDao.queryByPlazaIdAndUserId(plazaId, userId);
        if (exists == null) {
            aiPlazaFavorDao.insert(AiPlazaFavor.builder().plazaId(plazaId).userId(userId).build());
            aiPlazaDao.increaseFavorCount(plazaId, 1);
            return;
        }

        aiPlazaFavorDao.deleteByPlazaIdAndUserId(plazaId, userId);
        aiPlazaDao.increaseFavorCount(plazaId, -1);
    }

    @Override
    public void comment(Long userId, PlazaCommentRequest request) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(request.getPlazaId());
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        aiPlazaCommentDao.insert(AiPlazaComment.builder()
                .commentId(UUID.randomUUID().toString().replace("-", ""))
                .plazaId(request.getPlazaId())
                .userId(userId)
                .commentContent(request.getCommentContent())
                .commentStatus(1)
                .build());
        aiPlazaDao.increaseCommentCount(request.getPlazaId(), 1);
    }
}
