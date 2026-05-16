package member;

import java.time.LocalDate;
import java.time.Period;

import common.Grade;
import exception.ErrorCode;
import exception.ValidationException;
import member.MemberSignupDTO;

public class MemberService {

    private final MemberDAO memberDAO = new MemberDAO();

    // 회원가입
    public void signup(MemberSignupDTO dto) {

    	// 입력값이 비어있지 않은지 검증
        validateSignupInput(dto);

        if (memberDAO.existsByLoginId(dto.getLoginId())) {
            throw new ValidationException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (memberDAO.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new ValidationException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(dto.getPassword())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .phoneNumber(dto.getPhoneNumber())
                .grade(Grade.SILVER)
                .gradeSelectionDate(LocalDate.now())
                .isAdult(isAdult(dto.getBirthDate()))
                .build();

        memberDAO.insert(member);
    }

    // 회원가입 입력값 검증
    private void validateSignupInput(MemberSignupDTO dto) {

        if (dto == null) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        if (isBlank(dto.getLoginId())) {
            throw new ValidationException(ErrorCode.INVALID_LOGIN_ID);
        }

        if (isBlank(dto.getPassword())) {
            throw new ValidationException(ErrorCode.INVALID_PASSWORD);
        }

        if (isBlank(dto.getName())) {
            throw new ValidationException(ErrorCode.INVALID_MEMBER_NAME);
        }

        if (dto.getBirthDate() == null) {
            throw new ValidationException(ErrorCode.INVALID_BIRTH_DATE);
        }

        if (isBlank(dto.getPhoneNumber())) {
            throw new ValidationException(ErrorCode.INVALID_PHONE_NUMBER);
        }
    }

    // 생년월일 기준으로 성인 여부 판별
    private boolean isAdult(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 19;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}