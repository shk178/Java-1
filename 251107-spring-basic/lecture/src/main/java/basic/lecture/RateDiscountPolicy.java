package basic.lecture;

public class RateDiscountPolicy implements DiscountPolicy {
    public int discountPercent = 10;
    public int discountPrice(Member member, int price) {
        if (member.grade.equals("VIP"))
            return price * (discountPercent / 100);
        else
            return 0;
    }
}
