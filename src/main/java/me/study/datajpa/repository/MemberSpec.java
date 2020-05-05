package me.study.datajpa.repository;

import me.study.datajpa.entity.Member;
import me.study.datajpa.entity.Team;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class MemberSpec {

    public static Specification<Member> teamName(final String teamName) {
        return (Specification<Member>) (root, query, criteriaBuilder) -> {

            if (StringUtils.isEmpty(teamName)) {
                return null;
            }

            Join<Member, Team> t = root.join("team", JoinType.INNER);
            return criteriaBuilder.equal(t.get("name"), teamName);
        };
    }

    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, criteriaBuilder) -> {

            if (StringUtils.isEmpty(username)) {
                return null;
            }

            return criteriaBuilder.equal(root.get("username"), username);
        };
    }
}
