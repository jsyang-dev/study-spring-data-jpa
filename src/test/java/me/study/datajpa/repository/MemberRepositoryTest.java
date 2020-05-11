package me.study.datajpa.repository;

import me.study.datajpa.dto.MemberDto;
import me.study.datajpa.entity.Member;
import me.study.datajpa.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testMember() {

        Member member = new Member("MemberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {

        Member member1 = new Member("Member1");
        Member member2 = new Member("Member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {

        Member member1 = new Member("Member", 10);
        Member member2 = new Member("Member", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("Member", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("Member");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {

        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("Member1");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void testQuery() {

        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findMember("Member1", 10);
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void findUsernameList() {

        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {

        Team team = new Team("TeamA");
        teamRepository.save(team);

        Member member1 = new Member("Member1", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> memberDtoList = memberRepository.findMemberDto();
        for (MemberDto memberDto : memberDtoList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findByNames() {

        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("Member1", "Member2"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {

        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> listResult = memberRepository.findListByUsername("Member1");
        for (Member member : listResult) {
            System.out.println("member = " + member);
        }

        Member member = memberRepository.findMemberByUsername("Member1");
        System.out.println("member = " + member);

        Optional<Member> optionalResult = memberRepository.findOptionalByUsername("Member1");
        optionalResult.ifPresent(value -> System.out.println("optionalResult = " + value));
    }

    @Test
    public void findPageByAge() {

        // Given
        memberRepository.save(new Member("Member1", 10));
        memberRepository.save(new Member("Member2", 10));
        memberRepository.save(new Member("Member3", 10));
        memberRepository.save(new Member("Member4", 10));
        memberRepository.save(new Member("Member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // When
        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

        Page<MemberDto> memberDtoPage = page.map(member -> new MemberDto(member.getId(), member.getUsername(), member.getTeam().getName()));

        // Then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void findSliceByAge() {

        // Given
        memberRepository.save(new Member("Member1", 10));
        memberRepository.save(new Member("Member2", 10));
        memberRepository.save(new Member("Member3", 10));
        memberRepository.save(new Member("Member4", 10));
        memberRepository.save(new Member("Member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // When
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        // Then
        List<Member> content = slice.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(0);
        assertThat(slice.isFirst()).isTrue();
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    public void findListByAge() {

        // Given
        memberRepository.save(new Member("Member1", 10));
        memberRepository.save(new Member("Member2", 10));
        memberRepository.save(new Member("Member3", 10));
        memberRepository.save(new Member("Member4", 10));
        memberRepository.save(new Member("Member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // When
        List<Member> list = memberRepository.findListByAge(age, pageRequest);

        // Then
        assertThat(list.size()).isEqualTo(3);
    }

    @Test
    public void bulkUpdate() {

        // Given
        memberRepository.save(new Member("Member1", 10));
        memberRepository.save(new Member("Member2", 19));
        memberRepository.save(new Member("Member3", 20));
        memberRepository.save(new Member("Member4", 21));
        memberRepository.save(new Member("Member5", 40));

        // When
        int resultCount = memberRepository.bulkAgePlus(20);
//        em.clear();

        List<Member> result = memberRepository.findByUsername("Member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        // When
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {

        // Given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("Member1", 10, teamA);
        Member member2 = new Member("Member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // When
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    public void findMemberFetch() {

        // Given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("Member1", 10, teamA);
        Member member2 = new Member("Member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // When
        List<Member> members = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    public void findMemberEntityGraph() {

        // Given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("Member1", 10, teamA);
        Member member2 = new Member("Member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // When
        List<Member> members1 = memberRepository.findAll();
        for (Member member : members1) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        List<Member> members2 = memberRepository.findMemberEntityGraph();
        for (Member member : members2) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        List<Member> members3 = memberRepository.findEntityGraphByUsername("Member1");
        for (Member member : members3) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {

        // Given
        Member member = new Member("Member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        // When
        Member findMember = memberRepository.findReadOnlyByUsername("Member1");
        findMember.setUsername("Member2");

        em.flush();
    }

    @Test
    public void lock() {

        // Given
        Member member = new Member("Member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        // When
        List<Member> result = memberRepository.findLockByUsername("Member1");
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void specBasic() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("TeamA"));
        List<Member> result = memberRepository.findAll(spec);

        // Then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void queryByExample() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        Member member = new Member("m1");
        Team team = new Team("TeamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        // Then
        assertThat(result.get(0).getUsername()).isEqualTo("m1");
    }

    @Test
    public void projections() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }
    }

    @Test
    public void projectionsDto() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        List<UsernameOnlyDto> result = memberRepository.findProjectionsDtoByUsername("m1");

        for (UsernameOnlyDto usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }
    }

    @Test
    public void projectionsDynamic() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        List<Member> result = memberRepository.findProjectionsDynamicByUsername("m1", Member.class);

        for (Member usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }
    }

    @Test
    public void nestedClosedProjections() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        List<NestedClosedProjections> result = memberRepository.findProjectionsDynamicByUsername("m1", NestedClosedProjections.class);

        for (NestedClosedProjections nestedClosedProjections : result) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);
            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName = " + teamName);
        }
    }

    @Test
    public void nativeQuery() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        Member result = memberRepository.findByNativeQuery("m1");
        System.out.println("result = " + result);
    }

    @Test
    public void nativeProjection() {

        // Given
        Team teamA = new Team("TeamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // When
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.username = " + memberProjection.getUsername());
            System.out.println("memberProjection.teamName = " + memberProjection.getTeamName());
        }
    }
}