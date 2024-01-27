package com.kh.dog.member.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.dog.common.Pagination;
import com.kh.dog.main.model.service.ReservationService;
import com.kh.dog.main.model.vo.PageInfo;
import com.kh.dog.member.model.exception.MemberException;
import com.kh.dog.member.model.service.MemberService;
import com.kh.dog.member.model.vo.Dog;
import com.kh.dog.member.model.vo.Member;

@Controller
public class MemberController {
	private ArrayList<Map<String, Object>> dogArr;
	
	@Autowired	
	private MemberService mService;	
	
	@Autowired	
	private ReservationService rService;	
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@Autowired
	private JavaMailSender mailSender;
	
	// 회원가입
	@RequestMapping("enroll.me")
	public String enroll() {
	    dogArr = null;
	    return "enroll";
	}
	
	@RequestMapping("myPage.me")
	public String myPage(HttpSession session, Model model) {
		Member loginUser = ((Member)session.getAttribute("loginUser"));
		String id = loginUser.getUserId();
		ArrayList<Dog> dlist = rService.selectDog(id);

		model.addAttribute("m", loginUser);
		model.addAttribute("d", dlist);
		return "myPage";
	}

	@RequestMapping("insertMember.me")
	public String insertMember(@ModelAttribute Member m,
	                           @RequestParam("sample6_postcode") String sample6_postcode,
	                           @RequestParam("sample6_address") String sample6_address,
	                           @RequestParam("sample6_detailAddress") String sample6_detailAddress,
	                           @RequestParam("sample6_extraAddress") String sample6_extraAddress) {

	    String address = null;
	    if (!sample6_postcode.trim().equals("")) {
	        address = sample6_postcode + "@" + sample6_address + "@" + sample6_detailAddress + "@" + sample6_extraAddress;
	    }
	    m.setAddress(address);

	    m.setPwd(bcrypt.encode(m.getPwd()));
	    m.getUserId(); // 아이디 뽑아오래

	    if (dogArr != null) {
	        for (int i = 0; i < dogArr.size(); i++) {
	            dogArr.get(i).put("userId", m.getUserId());
	        }
	        System.out.println(dogArr);
	    }

	    int result = mService.insertMember(m);
	    if(dogArr != null) {
	    	mService.insertDog(dogArr);
		    dogArr = null;
	    }
	    if (result > 0) {
	        return "login";
	    } else {
	        throw new MemberException("회원가입을 실패하였습니다.");
	    }
	}
	
	// 로그인
	@RequestMapping("loginView.me")
	public String moveToLoginView() {
		return "login";
	}
	
	@RequestMapping("login.me")	
	public String login(Member m, Model model, HttpSession session) {			//	Model에 Attribute가 추가될 때 자동으로 키 값을 찾아 세션에 등록
		Member loginUser = mService.login(m);				// loginUser : 아이디가 일치했을때의 멤버 정보 / 암호화 된 비밀번호
		if(loginUser != null) {
			if(bcrypt.matches(m.getPwd(), loginUser.getPwd())) {	
				session.setAttribute("loginUser", loginUser);
				return "redirect:/";							
			} else {
				model.addAttribute("msg", "로그인에 실패하였습니다");
				return "login";
			}
		}else {
			model.addAttribute("msg", "로그인에 실패하였습니다.");
			return "login";
		}
		
	}
	
	// 반려견 정보 추가 팝업
	@RequestMapping("child.me")
    public String child(Model model, HttpSession session) {
		
		if(((Member)session.getAttribute("loginUser")) != null) {
			String id = ((Member)session.getAttribute("loginUser")).getUserId();
			ArrayList<Dog> list = rService.selectDog(id);
			if(list != null) {
				model.addAttribute("list", list);
			}
		}
		if(dogArr != null) {
			model.addAttribute("list", dogArr);
		}
		return "child";
    }
	
	@RequestMapping("insertChild.me")
	@ResponseBody
	public void insertChild(@RequestBody ArrayList<Map<String, Object>> dogList) {
		dogArr = dogList;
		System.out.println(dogList);
	}

	// 로그아웃 
	@RequestMapping("logout.me")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
	
	// 아이디 찾기
	@RequestMapping("findID.me")
	public String findId() {
		return "findId";
	}
	
	// 아이디 찾기
	@RequestMapping("selectFindId.me")
	public String selectFindId(@ModelAttribute Member m, Model model) {
		Member m1 = mService.selectFindId(m);
		System.out.println(m1);
		if(m1 != null) {
			model.addAttribute("m1", m1);
			return "searchSuccess";
		} else {
			model.addAttribute("m1", m1);
			return "searchSuccess";
		}
	}
	
	// 비밀번호 찾기 페이지	
	@RequestMapping("findPW.me")
	public String findPwd() {
		return "findPwd";
	}	
	// 비밀번호 찾기 - 이메일 인증 창	
	@RequestMapping("selectfindPwd.me")
	public String selectFindPwd(@ModelAttribute Member m, Model model) {
		int check = mService.selectFindPwd(m);
		
		if(check > 0) {
			model.addAttribute("m", m);
			return "changePwd";
		} else {
			model.addAttribute("msg","이메일 인증을 완료해주세요.");
	         return "findPwd";
		}
	}
		
	// 비밀번호 변경 창
	@RequestMapping("changePwd.me")
	public String changePwd(@ModelAttribute Member m, Model model) {
		System.out.println(m);
		m.setPwd(bcrypt.encode(m.getPwd()));	// 비밀번호 수정하면서 암호화 해줘야 디비에 바뀌어서 들어감
		int result = mService.changePwd(m);
		if(result > 0) {
			return "redirect:loginView.me";
		} else {
			model.addAttribute("msg", "비밀번호 변경 실패하였습니다.");
	        return "changePwd";		}
	}
	
	// 아이디 중복확인
	@RequestMapping("checkId.me")
	@ResponseBody
	public String checkId(@RequestParam("userId") String userId) {
		System.out.println(userId);
		int count = mService.checkId(userId);
		String result = count == 0 ? "yes" : "no";
		System.out.println(result);
		
		return result; 
	}
	
	// 내 정보 수정
	@RequestMapping("myInfo.me")
	public String editMyInfo(HttpSession session, Model model) {
		Member m = (Member)session.getAttribute("loginUser");
		
		model.addAttribute("loginUser", m);
		return "edit";
	}
	
	@RequestMapping("updateMember.me")
	public String updateMember(@ModelAttribute Member m, 
							   @RequestParam("sample6_postcode") String sample6_postcode,
				               @RequestParam("sample6_address") String sample6_address,
				               @RequestParam("sample6_detailAddress") String sample6_detailAddress,
				               @RequestParam("sample6_extraAddress") String sample6_extraAddress, Model model, HttpSession session) {
		Member loginUser = (Member)session.getAttribute("loginUser");
		m.setIsAdmin(loginUser.getIsAdmin());
		m.setJoinDate(loginUser.getJoinDate());
		m.setStatus(loginUser.getStatus());
		String address = null;
	    if (!sample6_postcode.trim().equals("")) {
	        address = sample6_postcode + "@" + sample6_address + "@" + sample6_detailAddress + "@" + sample6_extraAddress;
	    }
	    m.setAddress(address);
	    int result = mService.updateMember(m);
	    
	    if(dogArr != null) {
	    	mService.deleteDog(m.getUserId());
			if(dogArr != null) {
				for (int i = 0; i < dogArr.size(); i++) {
		            dogArr.get(i).put("userId", m.getUserId());
		        }
			}
			mService.insertDog(dogArr);
			dogArr = null;
	    }
		System.out.println(m);
		
		
		if(result > 0) {
			session.setAttribute("loginUser", m);
			return "redirect:myPage.me";
		} else {
			throw new MemberException("회원정보 수정 실패");
		}
	}
	
	// 반려견 정보 수정
	@RequestMapping("editDog.me")
	public String editDog(HttpSession session, Model model) {
		Member m = (Member)session.getAttribute("loginUser");
		
		model.addAttribute("loginUser", m);
		return "child";
	}
	
	// 페이징 처리 + 회원관리 + 조건 검색
	@RequestMapping("main.me")	
	public String memberAdmin(@RequestParam(value="page", defaultValue="1") int page, 
							  @RequestParam(value="searchType", defaultValue="") String searchType, 
							  @RequestParam(value="keyword", defaultValue="") String keyword, Model model) {
							  // keyword : 입력한 검색어 / searchType : select에서 가져오는 기준
		
		HashMap<String, String> map = new HashMap<>();
		map.put("searchType", searchType);
		map.put("keyword", keyword);
		
		
		int currentPage = page;
		int listCount = mService.getListCount(map);			
		
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 15);
		ArrayList<Member> list = mService.memberAdmin(pi, map);
		System.out.println(listCount);
		
		if(list != null) {
			model.addAttribute("pi", pi);
			model.addAttribute("list", list);
			return "main";
		} else {
			throw new MemberException("");
		}
	}
	
	// 회원 상세 조회
	@RequestMapping("memberDetail.me")
	public String selectMember(@RequestParam(value="userId") String userId, @RequestParam(value="page", defaultValue="1") String page, Model model, HttpSession session) {
		Member loginUser = mService.selectMember(userId);
		System.out.println(userId);
		
		
		// 주소
		String id = loginUser.getUserId();
	    String address = loginUser.getAddress();
	    String reAddress = "";
	    for(int i=0; i<address.split("@").length; i++) {
	       reAddress += address.split("@")[i]+" ";
	    }
	    loginUser.setAddress(reAddress);
	    model.addAttribute("loginUser", loginUser);
		
		return "memberDetail";
	}
	
	// 회원관리 삭제
	@RequestMapping("selectDelete.me")
	public String selectDelete(@RequestParam("deleteIds") String[] deleteIds) {
		
		mService.selectDelete(deleteIds);
		return "redirect:main.me";
	}

	// 회원 탈퇴	
	@RequestMapping("deleteMember.me")
	public String deleteMember(HttpSession session) {
		Member m = (Member)session.getAttribute("loginUser");
		String userId = m.getUserId();
		
		int result = mService.deleteMember(userId);
		if(result > 0) {
			return "redirect:logout.me";
		} else {
			throw new MemberException("회원 탈퇴 실패");
		}
	}
	
	// 이메일 인증
	@RequestMapping(value = "mailCheck.me", method = RequestMethod.GET ,produces = "aplication/json; charset=UTF-8")
	@ResponseBody
	public String sendMailTest(@RequestParam("Email") String to) throws Exception{
	        
		Random r = new Random();
	    int checkNum = r.nextInt(888888) + 111111;
	      
	    String subject = "인증코드"; 						// 제목
	    String content = "인증코드 "+checkNum+"입니다."; 	// 내용
	    String from = "ka7814@naver.com";
	  //String to = "mndwmktm@gmail.com";
	    System.out.println(to);
	    
	    try {
	        MimeMessage mail = mailSender.createMimeMessage();
	        MimeMessageHelper mailHelper = new MimeMessageHelper(mail,true,"UTF-8");
	        
	        mailHelper.setFrom(from); 					// 보낼사람 	
	        mailHelper.setTo(to); 						// 받을사람
	        mailHelper.setSubject(subject); 			// 제목
	        mailHelper.setText(content, true); 			// 내용
	            
	            
	        mailSender.send(mail);
	            
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	    	return checkNum+"";
	  }
}
