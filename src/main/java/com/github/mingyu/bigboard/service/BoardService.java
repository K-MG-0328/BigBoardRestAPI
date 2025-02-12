package com.github.mingyu.bigboard.service;

import com.github.mingyu.bigboard.dto.*;
import com.github.mingyu.bigboard.entity.Board;
import com.github.mingyu.bigboard.projection.BoardProjection;
import com.github.mingyu.bigboard.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    //게시글 생성
    public BoardDetailResponse createBoard(BoardDetailServiceRequest boardDetail) {
        Board board = boardDetail.toBoard();
        boardRepository.save(board);
        return BoardDetailResponse.toBoardDetailResponse(board);
    }

    //게시글 목록 조회 Redis 적용 전
    public Page<BoardProjection> getAllBoardsBefore(Pageable pageable) {
        log.info("getAllBoardsBefore");
        return boardRepository.findBoardAll(pageable);
    }

    //게시글 조회 Redis 적용 전
    public BoardDetailResponse getBoardByIdBefore(Long boardId){
        log.info("BoardService::getBoardByIdBefore");
        this.increaseViewCount(boardId);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        return BoardDetailResponse.toBoardDetailResponse(board);
    }

    @Transactional
    public void increaseViewCount(Long boardId) {
        boardRepository.incrementViewCount(boardId);
    }

    //게시글 수정
    public BoardDetailResponse updateBoard(BoardDetailServiceRequest updateBoard){
        Board board = boardRepository.findById(updateBoard.getBoardId()).orElse(null);
        board.setContent(updateBoard.getContent());
        board.setUpdatedAt(updateBoard.getUpdatedAt());
        boardRepository.save(board);
        return BoardDetailResponse.toBoardDetailResponse(board);
    }

    //게시글 삭제
    public void deleteBoard(Long boardId){
        boardRepository.deleteById(boardId);
    }

    //평점 추가 Redis 적용 전
    @Transactional
    public double updateBoardRating(BoardScoreServiceRequest boardScore){
        Board board = boardRepository.findById(boardScore.getBoardId()).orElse(null);
        board.setRatingCount(board.getRatingCount()+1);
        board.setTotalScore(board.getTotalScore() + boardScore.getScore());
        boardRepository.save(board);
        return board.getAverageScore();
    }
}
