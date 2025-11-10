package basic.lecture;

public class OrderServiceImpl implements OrderService {
    public final MemberRepository memberRepository;
    public final DiscountPolicy discountPolicy;
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    public Order createOrder(Long id, String name, int price) {
        Member member = memberRepository.findById(id);
        int discount = discountPolicy.discountPrice(member, price);
        return new Order(id, name, price, discount);
    }
}
