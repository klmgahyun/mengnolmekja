<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>공지사항</title>
    <link rel="stylesheet" href="resources/css/yoonseo-css/css/style.css">
    <script src="https://code.jquery.com/jquery-3.6.1.min.js"></script>
    <style>
    	.board_wrap{
    		min-height: 590px;
    	}
    	
    </style>
</head>
<body>

<jsp:include page="../../commons/Nav.jsp"/>
<jsp:include page="../../commons/sideBarBoard.jsp"/>
   <c:set value="${ pageContext.servletContext.contextPath }" var="contextPath"
	 scope="application"/>
    
    <div class="board_wrap">
        <div class="board_title">
            <strong>커뮤니티 게시판</strong>
            <p>후기 게시글은 총 ${ count }개 입니다.</p>
        </div>
        
        <div>
           <button class="boardBtn" id="review">후기</button>
           <button class="boardBtn" onclick="location.href='${contextPath}/list3.go'">자유</button>
           <button class="boardBtn" onclick="location.href='${contextPath}/list2.go'">질의응답</button>        
        </div>
        
        <div class="board_list_wrap">
            <div class="board_list">
                <div class="top">
                    <div class="num" >글번호</div>
                    <div class="title" >제목</div>
                    <div class="writer" >작성자</div>
                    <div class="date" >작성일</div>
                    <div class="count">조회</div>
                    
                </div>
               
               <tbody>
	                <c:forEach items="${list }" var="b">
		                <div class="list-select">
		                    <div class="num">${b.boardNo }</div>
		                    <div class="title">${b.title }</div>
		                    <div class="writer">${b.userId }</div>
		                    <div class="date">${b.createDate }</div>
		                    <div class="count">${b.boardView }</div>
		                    
		                </div>
	                </c:forEach> 
              </tbody>  
           </div>
        </div>
        <div class="board_page">
		      <c:url var="goFirst" value="${ loc }">
		         <c:param name="page" value="${ pi.startPage }" />
		      </c:url>
		
		      <a href="${ goFirst }" class="btn frist"><<</a>
		
		      <c:if test="${ pi.currentPage > 1}">
			      <c:url var="goBack" value="${ loc }">
			         <c:param name="page" value="${ pi.currentPage-1 }" />
			      </c:url>
		      </c:if>
		      
		      <a href="${ goBack }" class="btn prew"><</a>
		
		      <c:forEach begin="${ pi.startPage }" end="${ pi.endPage }" var="p">
		         <c:url var="goNum" value="${ loc }">
		            <c:param name="page" value="${ p }" />
		         </c:url>
		         <c:if test="${ pi.currentPage eq  p }">
		            <a href="${ goNum }" class="num selected">${ p }</a>
		         </c:if>
		         <c:if test="${ pi.currentPage ne  p }">
		            <a href="${ goNum }" class="num">${ p }</a>
		         </c:if> 
		      </c:forEach>
		      
		      <c:if test="${ pi.currentPage <= endPage}">
			      <c:url var="goNext" value="${ loc }">
			         <c:param name="page" value="${ pi.currentPage+1 }" />
			      </c:url>
		      </c:if>
		      
		      <a href="${ goNext }" class="btn next">></a>
		      
		      <c:url var="goLast" value="${ loc }">
		         <c:param name="page" value="${ pi.endPage }" />
		      </c:url>
		      <a href="${ goLast }" class="btn last">>></a>
        </div>
           
           <c:if test="${!empty loginUser}">
	           <div class="bt_wrap">
	                    <button  onclick="location.href='${ contextPath }/write.go'" id="sub-btn" class="on">등록</button>   
	           </div>
           </c:if>
             
        <form action="searchBoard.go" method="get">     
            <div class="search">
               <select id="selectSearch" name="select">
                  <option>아이디검색</option>
                  <option>제목검색</option>
                  <option>내용검색</option>
               </select>
               <input id="searchInput" type="text" placeholder="검색어를 입력하세요" name="keyword">
               <button class="seach_btn">검색</button>
            </div>
        </form>
    
    
    <script>
       window.onload = ()=> {
    	   /* const btn = document.getElementById('sub-btn');
    	   btn.addEventListener('click', ()=>{
    		   location.href='${ contextPath }/write.go';
    	   })
    	   btn.addEventListener('mouseover',()=>{
    		   btn.style.backgroundColor='#F8DE22';
    	   })
    	   btn.addEventListener('mouseout', ()=> {
    		   btn.style.backgroundColor='#FFD966';
    	   }) */
    	   
    	   const parentDivs = document.getElementsByClassName('list-select');
    	   for (const parentDiv of parentDivs) {
    		   parentDiv.addEventListener('click', function (){
    			   const divs = parentDiv.children;
    			   const boardNo = divs[0].innerText;
    			   console.log(boardNo);
    			   location.href = '${ contextPath }/selectBoard.go?boardNo='
    					             + boardNo + '&page=' + ${pi.currentPage};
    		   })
    	       
    	   }
    		   
    	  
    	   }
    	   
    	   
    	   
    	   
       
    </script>
   <jsp:include page="../../commons/footer.jsp"/>
    
</body>
</html> 