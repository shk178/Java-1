### ExecutorService - 작업 컬렉션 처리
- invokeAll(tasks)
- 모든 Callable 작업을 제출하고, 모든 작업이 완료될 때까지 기다린다.
- invokeAll(tasks, timeout, unit)
- 지정된 시간 내에 모든 Callable 작업을 제출하고 완료될 때까지 기다린다.
- invokeAny(tasks)
- 하나의 Callable 작업이 완료될 때까지 기다리고, 가장 먼저 완료된 작업의 결과를 반환한다.
- 완료되지 않은 나머지 작업은 취소한다.
- invokeAny(tasks, timeout, unit)
- 지정된 시간 내에 하나의 Callable 작업이 완료될 때까지 기다리고, 가장 먼저 완료된 작업의 결과를 반환한다.
- 완료되지 않은 나머지 작업은 취소한다.
- 251011-java-adv1/src/executor/InvokeMain.java
```java
/*
21:41:22.682 [pool-1-thread-2] B 시작
21:41:22.682 [pool-1-thread-1] A 시작
21:41:22.682 [pool-1-thread-3] C 시작
21:41:23.704 [pool-1-thread-1] A 완료, return=1000
21:41:24.690 [pool-1-thread-2] B 완료, return=2000
21:41:25.697 [pool-1-thread-3] C 완료, return=3000
21:41:25.697 [     main] result=1000
21:41:25.697 [     main] result=2000
21:41:25.697 [     main] result=3000
21:41:25.700 [pool-1-thread-1] D 시작
21:41:25.700 [pool-1-thread-3] F 시작
21:41:25.700 [pool-1-thread-2] E 시작
21:41:29.710 [pool-1-thread-1] D 완료, return=4000
21:41:29.710 [pool-1-thread-1] D 시작
21:41:30.709 [pool-1-thread-2] E 완료, return=5000
21:41:30.709 [pool-1-thread-2] E 시작
21:41:31.708 [pool-1-thread-3] F 완료, return=6000
21:41:31.709 [pool-1-thread-3] F 시작
21:41:33.714 [pool-1-thread-1] D 완료, return=4000
//submit으로 받은 Future는
//invokeAny가 내부적으로 생성한 Future와 다른 객체다.
21:41:33.716 [     main] result=4000
21:41:33.716 [pool-1-thread-3] 인터럽트 발생: sleep interrupted
21:41:33.716 [pool-1-thread-2] 인터럽트 발생: sleep interrupted
//invokeAny하면 내부적으로 Future 만들어서 전부 실행하고
//그중 하나가 완료하면 나머지를 cancel(true)한다.
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
//submit으로 받은 Future은 별개의 Future다.
//cancel 호출이 전파되지 않는다.
- ExecutorService는 풀에 있는 워커 스레드를 활용해 모든 작업을 병렬로 실행합니다.
- 가장 먼저 call()을 성공적으로 완료한 작업의 결과를 반환합니다.
- 나머지 작업은 cancel(true)로 인터럽트를 걸어 중단시킵니다.
 */
```
- 즉, ExecutorService에서 단일 실행:
- submit(Callable / Runnable 다 쓸 수 있다. Runnable 결과 저장할 인자도 넘길 수 있다.)
- 다수 실행: invokeAll, invokeAny