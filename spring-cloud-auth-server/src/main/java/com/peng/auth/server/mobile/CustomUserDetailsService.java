package com.peng.auth.server.mobile;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.stereotype.Service;

import com.peng.auth.server.repository.Member;
import com.peng.auth.server.repository.MemberRepository;



//创建用户细节服务
@Service
public class CustomUserDetailsService {
	
	@Autowired
	private MemberRepository memberRepository;
	
	public UserDetails loadUserByPhoneAndPassword(String phone, String password) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw new InvalidGrantException("无效的手机号或密码");
        }
        Member member = memberRepository.findByUsername(phone);
        member.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
        // 判断成功后返回用户细节
        return member;
    }

    public UserDetails loadUserByPhoneAndSmsCode(String phone, String smsCode) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(smsCode)) {
            throw new InvalidGrantException("无效的手机号或短信验证码");
        }
        Member member = memberRepository.findByUsername(phone);
        //判断短信是否正确
        member.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
        return member;
    }
}
