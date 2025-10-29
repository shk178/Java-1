package lambda3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MethodSignatureExtractor {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("C:/Users/user/Documents/GitHub/Java-1/251028-java-adv3/src/lambda3/reference/function");
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(MethodSignatureExtractor::processFile);
        }
    }
    private static void processFile(Path filePath) {
        System.out.println("File: " + filePath.getFileName());
        try {
            String content = Files.readString(filePath);
            // 메서드 시그니처 정규식
            // (public|protected|private)? : 접근 제어자 (선택적, ? = 0개 또는 1개)
            // \\s*                        : 공백 문자 0개 이상
            // (static)?                   : static 키워드 (선택적)
            // \\s*                        : 공백 문자 0개 이상
            // (default)?                  : default 키워드 (선택적)
            // \\s*                        : 공백 문자 0개 이상
            // \\S+                        : 반환 타입 (공백이 아닌 문자 1개 이상)
            // \\s+                        : 공백 문자 1개 이상 (반환타입과 메서드명 구분)
            // \\w+                        : 메서드 이름 (단어 문자 1개 이상)
            // \\s*                        : 공백 문자 0개 이상
            // \\(                         : 여는 괄호 (
            // [^)]*                       : 닫는 괄호가 아닌 모든 문자 0개 이상 (매개변수)
            // \\)                         : 닫는 괄호 )
            // \\s*                        : 공백 문자 0개 이상
            // (\\{|;)                     : { 또는 ; (메서드 구현 시작 또는 추상 메서드 끝)
            Pattern methodPattern = Pattern.compile(
                    "(public|protected|private)?\\s*(static)?\\s*(default)?\\s*\\S+\\s+\\w+\\s*\\([^)]*\\)\\s*(\\{|;)"
            );
            Matcher matcher = methodPattern.matcher(content);
            while (matcher.find()) {
                String method = matcher.group().trim();
                System.out.println(method);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        }
        System.out.println();
    }
}
/*
File: BiConsumer.java
void accept(T t, U u);
U> andThen(BiConsumer<? super T, ? super U> after) {
{
            accept(l, r);

File: BiFunction.java
R apply(T t, U u);
V> andThen(Function<? super R, ? extends V> after) {

File: BinaryOperator.java
BinaryOperator<T> minBy(Comparator<? super T> comparator) {
BinaryOperator<T> maxBy(Comparator<? super T> comparator) {

File: BiPredicate.java
boolean test(T t, U u);
U> and(BiPredicate<? super T, ? super U> other) {
U> negate() {
U> or(BiPredicate<? super T, ? super U> other) {

File: BooleanSupplier.java
boolean getAsBoolean();

File: Consumer.java
void accept(T t);
default Consumer<T> andThen(Consumer<? super T> after) {
{ accept(t);

File: DoubleBinaryOperator.java
double applyAsDouble(double left, double right);

File: DoubleConsumer.java
void accept(double value);
default DoubleConsumer andThen(DoubleConsumer after) {
{ accept(t);

File: DoubleFunction.java
R apply(double value);

File: DoublePredicate.java
boolean test(double value);
default DoublePredicate and(DoublePredicate other) {
default DoublePredicate negate() {
default DoublePredicate or(DoublePredicate other) {

File: DoubleSupplier.java
double getAsDouble();

File: DoubleToIntFunction.java
int applyAsInt(double value);

File: DoubleToLongFunction.java
long applyAsLong(double value);

File: DoubleUnaryOperator.java
double applyAsDouble(double operand);
default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
static DoubleUnaryOperator identity() {

File: Function.java
R apply(T t);
R> compose(Function<? super V, ? extends T> before) {
V> andThen(Function<? super R, ? extends V> after) {
T> identity() {

File: IntBinaryOperator.java
int applyAsInt(int left, int right);

File: IntConsumer.java
void accept(int value);
default IntConsumer andThen(IntConsumer after) {
{ accept(t);

File: IntFunction.java
R apply(int value);

File: IntPredicate.java
boolean test(int value);
default IntPredicate and(IntPredicate other) {
default IntPredicate negate() {
default IntPredicate or(IntPredicate other) {

File: IntSupplier.java
int getAsInt();

File: IntToDoubleFunction.java
double applyAsDouble(int value);

File: IntToLongFunction.java
long applyAsLong(int value);

File: IntUnaryOperator.java
int applyAsInt(int operand);
default IntUnaryOperator compose(IntUnaryOperator before) {
default IntUnaryOperator andThen(IntUnaryOperator after) {
static IntUnaryOperator identity() {

File: LongBinaryOperator.java
long applyAsLong(long left, long right);

File: LongConsumer.java
void accept(long value);
default LongConsumer andThen(LongConsumer after) {
{ accept(t);

File: LongFunction.java
R apply(long value);

File: LongPredicate.java
boolean test(long value);
default LongPredicate and(LongPredicate other) {
default LongPredicate negate() {
default LongPredicate or(LongPredicate other) {

File: LongSupplier.java
long getAsLong();

File: LongToDoubleFunction.java
double applyAsDouble(long value);

File: LongToIntFunction.java
int applyAsInt(long value);

File: LongUnaryOperator.java
long applyAsLong(long operand);
default LongUnaryOperator compose(LongUnaryOperator before) {
default LongUnaryOperator andThen(LongUnaryOperator after) {
static LongUnaryOperator identity() {

File: ObjDoubleConsumer.java
void accept(T t, double value);

File: ObjIntConsumer.java
void accept(T t, int value);

File: ObjLongConsumer.java
void accept(T t, long value);

File: Predicate.java
boolean test(T t);
default Predicate<T> and(Predicate<? super T> other) {
default Predicate<T> negate() {
default Predicate<T> or(Predicate<? super T> other) {
Predicate<T> isEqual(Object targetRef) {
Predicate<T> not(Predicate<? super T> target) {

File: Supplier.java
T get();

File: ToDoubleBiFunction.java
double applyAsDouble(T t, U u);

File: ToDoubleFunction.java
double applyAsDouble(T value);

File: ToIntBiFunction.java
int applyAsInt(T t, U u);

File: ToIntFunction.java
int applyAsInt(T value);

File: ToLongBiFunction.java
long applyAsLong(T t, U u);

File: ToLongFunction.java
long applyAsLong(T value);

File: UnaryOperator.java
UnaryOperator<T> identity() {
 */