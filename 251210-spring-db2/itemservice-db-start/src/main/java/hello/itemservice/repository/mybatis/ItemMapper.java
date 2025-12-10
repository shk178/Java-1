package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {
    void save(Item item);
    void update(
            @Param("id") Long id,
            @Param("updateParam") ItemUpdateDto updateParam
            );
    Optional<Item> findById(Long id);
    List<Item> findAll(ItemSearchCond cond);
}
/*
MyBatis
동적 쿼리 (if, choose, trim, foreach)
annotation으로 쿼리 작성 @Select("select ~ where id = #{id}")
${} (문자열 대체 - 인젝션 주의)
<sql> 코드 -> <include>로 사용
결과 매핑 시 as로 별칭 지정 또는 resultMap 선언
 */