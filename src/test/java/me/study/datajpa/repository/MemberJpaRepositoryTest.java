package me.study.datajpa.repository;

import me.study.datajpa.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember() {
        Member member = new Member("MemberA");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(member.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("Member1");
        Member member2 = new Member("Member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("Member", 10);
        Member member2 = new Member("Member", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("Member", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("Member");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member member1 = new Member("Member1", 10);
        Member member2 = new Member("Member2", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> result = memberJpaRepository.findByUsername("Member1");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void paging() {
        // Given
        memberJpaRepository.save(new Member("Member1", 10));
        memberJpaRepository.save(new Member("Member2", 10));
        memberJpaRepository.save(new Member("Member3", 10));
        memberJpaRepository.save(new Member("Member4", 10));
        memberJpaRepository.save(new Member("Member5", 10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        // When
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        // Then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }
}