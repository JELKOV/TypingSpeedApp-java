package com.bungeobbang.app.biz.board;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.bungeobbang.app.biz.filterSearch.BoardFilter;

@Repository
public class BoardDAO {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	// INSERT 쿼리들
	// 게시글 작성
	private final String INSERT = 
			"INSERT INTO BB_BOARD(BOARD_TITLE, BOARD_CONTENT, BOARD_FOLDER, BOARD_OPEN, BOARD_CATEGORY_NUM, MEMBER_NUM) " +
					"VALUES(?,?,?,?,?,?)";

	// 상품 게시글 작성
	private final String INSERT_PRODUCT = 
			"INSERT INTO BB_BOARD(PRODUCT_NUM, BOARD_TITLE, BOARD_CONTENT, BOARD_FOLDER, BOARD_CATEGORY_NUM) " +
					"VALUES(?,?,?,?,?)";

	// 상점 게시글 작성
	private final String INSERT_STORE = 
			"INSERT INTO BB_BOARD(BOARD_TITLE, BOARD_CONTENT, BOARD_FOLDER, STORE_NUM) " +
					"VALUES(?,?,?,?)";

	// UPDATE 쿼리들
	private final String UPDATE = 
			"UPDATE BB_BOARD SET BOARD_TITLE = ?, BOARD_CONTENT = ?, BOARD_OPEN = ? " +
					"WHERE BOARD_NUM = ?";

	private final String UPDATE_DEL = 
			"UPDATE BB_BOARD SET BOARD_DELETE = 'Y' WHERE BOARD_NUM = ?";

	// DELETE 쿼리
	private final String DELETE = "DELETE FROM BB_BOARD WHERE BOARD_NUM = ?";

	// SELECTALL 쿼리	
	private final String SELECTALL = 
			"SELECT @rownum := @rownum + 1 AS RN, BOARD_NUM, BOARD_TITLE, BOARD_CONTENT, " 
					+"BOARD_OPEN, BOARD_DELETE, BOARD_WRITE_DAY, BOARD_CATEGORY_NUM, BOARD_CATEGORY_NAME, "
					+"MEMBER_NUM, MEMBER_NICKNAME, MEMBER_PROFILE_WAY, LIKE_CNT, REPLY_CNT, BOARD_FOLDER "
					+"FROM BB_VIEW_BOARD_JOIN, (SELECT @rownum := 0) AS r "
					+"WHERE 1=1 ";			

	private final String SELECTALL_ENDPART = 
			"ORDER BY BOARD_NUM DESC " 
					+"LIMIT ?, ?";

	// SELECTALL_HOT 쿼리
	private final String SELECTALL_HOT = 
			"SELECT BOARD_NUM, BOARD_TITLE, BOARD_CONTENT, BOARD_OPEN, BOARD_DELETE, " 
					+"BOARD_CATEGORY_NUM, BOARD_CATEGORY_NAME, MEMBER_NUM, MEMBER_NICKNAME, MEMBER_PROFILE_WAY, "
					+"BOARD_WRITE_DAY, LIKE_CNT, REPLY_CNT, BOARD_FOLDER "
					+"FROM BB_VIEW_BOARD_JOIN " 
					+"WHERE BOARD_CATEGORY_NUM = ? AND LIKE_CNT >= ? "
					+"ORDER BY LIKE_CNT DESC LIMIT ? ";

	// SELECTALL_MYPAGE 쿼리
	private final String SELECTALL_MYPAGE = 
			"SELECT BOARD_NUM, BOARD_TITLE, BOARD_CONTENT, " +
					"BOARD_OPEN, BOARD_DELETE, BOARD_CATEGORY_NUM, BOARD_CATEGORY_NAME, MEMBER_NUM, " +
					"MEMBER_NICKNAME, BOARD_WRITE_DAY, MEMBER_PROFILE_WAY, LIKE_CNT, REPLY_CNT, BOARD_FOLDER " +
					"FROM BB_VIEW_BOARD_JOIN " +
					"WHERE MEMBER_NUM = ? " +
					"ORDER BY BOARD_WRITE_DAY DESC LIMIT ?, ?";

	// SELECTONE 쿼리
	private final String SELECTONE = 
			"SELECT BOARD_NUM, BOARD_TITLE, BOARD_CONTENT, BOARD_OPEN, BOARD_DELETE, "
					+"BOARD_CATEGORY_NUM, BOARD_CATEGORY_NAME, MEMBER_NUM, MEMBER_NICKNAME, MEMBER_PROFILE_WAY, "
					+"BOARD_WRITE_DAY, LIKE_CNT, REPLY_CNT, BOARD_FOLDER " +
					"FROM BB_VIEW_BOARD_JOIN WHERE BOARD_NUM = ?";

	// SELECTONE_SEARCH 쿼리
	private final String SELECTONE_SEARCH = 
			"SELECT COUNT(*) AS CNT FROM BB_BOARD bb " +
					"JOIN BB_MEMBER bm ON bb.MEMBER_NUM = bm.MEMBER_NUM " +
					"WHERE 1=1 ";

	// SELECTONE_MAXPK 쿼리
	private final String SELECTONE_MAXPK = "SELECT IFNULL(MAX(BOARD_NUM), 0) AS MAXPK FROM BB_BOARD";

	private final String SELECTONE_FOLDER = "SELECT BOARD_NUM, BOARD_FOLDER FROM BB_BOARD WHERE BOARD_NUM = ?";

	private final String SELECTONE_MY = "SELECT COUNT(*) AS CNT FROM BB_BOARD WHERE MEMBER_NUM = ?";

	//고정 설정
	private final int MINLIKE = 5;		//인기글 최소 기준
	private final int SHOWHOTBOARD = 3;	//보여줄 인기글 개수
	private final int PRODUCT = 3; // 상품 카테고리 번호
	private final int CONTENT_SIZE = 10; // 페이지당 게시글 수


	public boolean insert(BoardDTO boardDTO) {
		System.out.println("log: Board insert start");
		System.out.println("boardDTO = "+boardDTO);
		//결과를 담을 변수
		int rs=0;
		String query ="";
		try {
			if(boardDTO.getCondition().equals("BOARD_INSERT")) {
				//새 게시글 작성
				System.out.println("log : Board insert : BOARD_INSERT");
				query = INSERT;
				rs = jdbcTemplate.update(query,
						boardDTO.getBoardTitle(),
						boardDTO.getBoardContent(),
						boardDTO.getBoardFolder(),
						boardDTO.getBoardOpen(),
						boardDTO.getBoardCategoryNum(),
						boardDTO.getMemberNum()
						);
			}
			else if(boardDTO.getCondition().equals("PRODUCT_INSERT")) {
				//상품설명용 게시글 작성
				System.out.println("log: Board insert : PRODUCT_INSERT");
				query=INSERT_PRODUCT;
				rs=jdbcTemplate.update(query,
						boardDTO.getProductNum(),
						boardDTO.getBoardTitle(),
						boardDTO.getBoardContent(),
						boardDTO.getBoardFolder(),
						PRODUCT //상품 카테고리 번호
						);
			}
			else if(boardDTO.getCondition().equals("MARKET_INSERT")) {
				// 가게 게시글 등록
				System.out.println("log : Board insert : MARKET_INSERT");
				query = INSERT_STORE;
				rs = jdbcTemplate.update(query,
						boardDTO.getBoardTitle(),
						boardDTO.getBoardContent(),
						boardDTO.getBoardFolder(),
						boardDTO.getStoreNum()
						);
			}
			else {
				//컨디션값 오류
				System.err.println("log: Board insert condition fail");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		// 쿼리 실행
		if(rs <=0) {
			System.err.println("log: Board insert fail");
			return false;
		}
		System.out.println("log: Board insert success");
		return true;
	}
	public boolean update(BoardDTO boardDTO) {
		System.out.println("log: Board update start");
		String query = "";
		Object[] args = null;
		int rs=0;
		if(boardDTO.getCondition().equals("BOARD_UPDATE")) {
			//게시글 수정
			System.out.println("log: Board update : BOARD_UPDATE");		
			query = UPDATE;
			args = new Object[] {
					boardDTO.getBoardTitle(),
					boardDTO.getBoardContent(),
					boardDTO.getBoardOpen(),
					boardDTO.getBoardNum()
			};
		}
		else if(boardDTO.getCondition().equals("ADMIN_DELETE")) {
			//관리자 삭제
			System.out.println("log: Board update : ADMIN_DELETE");
			query = UPDATE_DEL;
			args = new Object[] {
					boardDTO.getBoardNum()
			};
		}
		else {
			System.err.println("log: Board update condition fail");
		}

		//쿼리 실행
		try {
			rs = jdbcTemplate.update(query, args);
		}catch (Exception e) {
			System.err.println("log: Board update Exception fail :"+ e.getMessage());
			return false;
		}

		if(rs <=0) {
			System.err.println("log: Board update fail");
			return false;
		}
		System.out.println("log: Board update success");
		return true;
	}
	public boolean delete(BoardDTO boardDTO) {
		System.out.println("log: Board delete start");
		int rs = 0;
		Object[] args = null;
		String query = "";
		args = new Object[] {
				boardDTO.getBoardNum()
		};
		query = DELETE;

		try {
			rs = jdbcTemplate.update(query, args);
		}catch (Exception e) {
			System.err.println("log: Board update Exception fail :"+ e.getMessage());
			return false;
		}

		if(rs <=0) {
			System.err.println("log: Board update fail");
			return false;
		}
		System.out.println("log: Board update success");
		return true;
	}
	public List<BoardDTO> selectAll(BoardDTO boardDTO){
		List<BoardDTO> datas = new ArrayList<BoardDTO>();
		Object[] args=null; // 입력값 배열
		String query = ""; //쿼리문 초기화

		if(boardDTO.getCondition().equals("FILTER_BOARD")) {
			//게시물 리스트 (+필터검색)
			System.out.println("log: Board selectAll : FILTER_BOARD");
			//넘어온 MAP filter키워드
			HashMap<String, String> filters = boardDTO.getFilterList();
			//필터 객체 생성
			BoardFilter filterUtil = new BoardFilter();
			//쿼리문 생성
			query = filterUtil.buildFilterQuery(SELECTALL, filters).append(" "+SELECTALL_ENDPART).toString();

			// 키워드를 넣을 배열
			List<Object> argsList = new ArrayList<>();

			argsList = filterUtil.setFilterKeywords(argsList, filters);

			//페이지네이션
			argsList.add(boardDTO.getStartNum() < 1 ? 0 : boardDTO.getStartNum()-1);
			argsList.add(CONTENT_SIZE);

			//args 배열화
			args = argsList.toArray();
			System.out.println("log : args ="+Arrays.toString(args));
			// 쿼리 실행
			try {
				datas = jdbcTemplate.query(query, args, new BoardRowMapper());
			}
			catch(Exception e){
				System.err.println("log : Board selectAll : FILTER_BOARD fail");
				e.printStackTrace();
				return null;
			}
			return datas;
		}
		else if(boardDTO.getCondition().equals("HOT_BOARD")) {
			//게시판 별 인기글
			System.out.println("log: Board selectAll : HOT_BOARD");
			args=new Object[] {
					boardDTO.getBoardCategoryNum(),
					MINLIKE,
					SHOWHOTBOARD
			};
			query = SELECTALL_HOT;
		}
		else if(boardDTO.getCondition().equals("MY_BOARD")) {
			//내 게시글 모아보기
			System.out.println("log: Board selectAll : MY_BOARD");
			args = new Object[] {
					boardDTO.getMemberNum(),
					boardDTO.getStartNum() < 1? 0: boardDTO.getStartNum()-1,
					CONTENT_SIZE
			};
			query = SELECTALL_MYPAGE;
		}
		else {
			//컨디션값 오류
			System.err.println("log: Board selectAll condition fail");
		}
		datas = jdbcTemplate.query(query, args, new BoardRowMapper());
		System.out.println("log: Board selectAll return datas");
		return datas;
	}
	public BoardDTO selectOne(BoardDTO boardDTO) {
		System.out.println("log: Board selectOne start");
		Object[] args = null;
		String query="";

		if(boardDTO.getCondition().equals("ONE_BOARD")) {			
			//게시물 상세보기
			query = SELECTONE;
			args = new Object[]{
					boardDTO.getBoardNum()
			};
		}
		else if(boardDTO.getCondition().equals("CNT_BOARD")) {
			//게시판 별 글 개수(+필터검색)
			System.out.println("log: Board selectOne : CNT_BOARD");
			//필터검색 추가
			BoardDTO data = new BoardDTO();
			List<Object> argsList = new ArrayList<Object>();
			HashMap<String, String> filters = boardDTO.getFilterList();
			//넘어온 MAP filter키워드
			BoardFilter filterUtil = new BoardFilter();
			query = filterUtil.buildFilterQuery(SELECTONE_SEARCH,filters).toString();
			//argsList.add(boardDTO.getBoardCategoryNum());
			//카테고리 명

			//넘어온 값 확인 로그
			System.out.println("log: parameter getBoardCateNum : "+boardDTO.getBoardCategoryNum());
			filterUtil.setFilterKeywords(argsList, filters);		//필터 검색 검색어 
			// args 배열화
			args = argsList.toArray();
			// 쿼리 실행
			try {
				return jdbcTemplate.queryForObject(query, args, new CntRowMapper());
			}catch(Exception e) {
				System.err.println("log : CNT_BOARD fail");
				e.printStackTrace();
				return null;
			}
		}
		else if(boardDTO.getCondition().equals("MAXPK_BOARD")) {
			//가장 최근 PK
			System.out.println("log: Board selectOne : MAXPK_BOARD");
			query = SELECTONE_MAXPK;
			return jdbcTemplate.queryForObject(query, new MaxRowMapper());			
		}
		else if(boardDTO.getCondition().equals("FOLDER_BOARD")) {
			//게시판 번호로 폴더 검색
			System.out.println("log: Board selectOne : FOLDER_BOARD");
			query = SELECTONE_FOLDER;
			args = new Object[] {
					boardDTO.getBoardNum()
			};
			return jdbcTemplate.queryForObject(query, args, new FolderRowMapper());
		}
		else if(boardDTO.getCondition().equals("MY_BOARD")) {
			System.out.println("log : Board selectOne : MY_BOARD");
			query = SELECTONE_MY;
			args = new Object[] {
					boardDTO.getMemberNum()
			};
			return jdbcTemplate.queryForObject(query, args, new CntRowMapper());
		}
		else {
			//컨디션값 오류
			System.err.println("log: Board selectOne condition fail");
		}
		return jdbcTemplate.queryForObject(query, args, new BoardRowMapper());
	}

	// 회원 조회 selectAll RowMapper
	class BoardRowMapper implements RowMapper<BoardDTO>{

		@Override
		public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoardDTO data = new BoardDTO();
			data.setBoardNum(rs.getInt("BOARD_NUM")); 				//게시글 번호
			data.setBoardTitle(rs.getString("BOARD_TITLE")); 		//제목
			data.setBoardContent(rs.getString("BOARD_CONTENT")); 	//내용
			data.setBoardOpen(rs.getString("BOARD_OPEN")); 			//공개여부
			data.setBoardCategoryNum(rs.getInt("BOARD_CATEGORY_NUM")); 		//카테고리 번호
			data.setBoardCategoryName(rs.getString("BOARD_CATEGORY_NAME")); 	//카테고리 명
			data.setMemberNum(rs.getInt("MEMBER_NUM")); 			//멤버 PK
			data.setMemberNickname(rs.getString("MEMBER_NICKNAME")); //멤버 닉네임
			data.setMemberProfileWay(rs.getString("MEMBER_PROFILE_WAY")); //회원 프로필 사진
			data.setBoardWriteDay(rs.getString("BOARD_WRITE_DAY")); //작성일자
			data.setBoardDelete(rs.getString("BOARD_DELETE")); 		//관리자 글 삭제여부
			data.setBoardFolder(rs.getString("BOARD_FOLDER")); //폴더
			data.setLikeCnt(rs.getInt("LIKE_CNT")); //좋아요 수
			data.setReplyCnt(rs.getInt("REPLY_CNT")); //댓글 수
			System.out.print(" | result "+data.getBoardNum());
			return data;
		}

	}
	// 가장 최근 게시물
	class MaxRowMapper implements RowMapper<BoardDTO>{

		@Override
		public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoardDTO data = new BoardDTO();
			data.setMaxPk(rs.getInt("MAXPK"));
			return data;
		}		
	}
	// 게시글 번호와 폴더명 반환
	class FolderRowMapper implements RowMapper<BoardDTO>{

		@Override
		public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoardDTO data = new BoardDTO();
			data.setBoardNum(rs.getInt("BOARD_NUM"));
			data.setBoardFolder(rs.getString("BOARD_FOLDER"));
			System.out.println("log : result exists");
			return data;
		}

	}
	//게시글 개수
	class CntRowMapper implements RowMapper<BoardDTO>{
		@Override
		public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoardDTO data = new BoardDTO();
			data.setCnt(rs.getInt("CNT")); 
			return data;
		}

	}
}
