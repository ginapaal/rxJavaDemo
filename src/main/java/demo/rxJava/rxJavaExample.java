package demo.rxJava;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class rxJavaExample {

    public static void main(String[] args) {

        Observable.create(subscriber -> {
            subscriber.onNext("Hello World!");
            subscriber.onCompleted();
        }).subscribe(System.out::println);

        // shortened
        Observable.just("Hello", "World!")
                .subscribe(System.out::println);


        // add onError and onComplete listeners
        Observable.just("Hello World!")
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!"));


        Observable.create(new OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello World!");
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("Done!");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occured! " + throwable.getMessage());
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        });

        Observable.create(subscriber -> {
            try {
                subscriber.onNext("Hello World!");
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribe(System.out::println);

        //add concurrency manually
        Observable.create(subscriber -> {
            new Thread(() -> {
                try {
                    subscriber.onNext(getData());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).start();
        }).subscribe(System.out::println);

        // add concurrency (using a Scheduler)
        Observable.create(subscriber -> {
            try {
                subscriber.onNext(getData());
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(System.out::println);

        // add operator
        Observable.create(subscriber -> {
            try {
                subscriber.onNext(getData());
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .map(data -> data + " --> at " + System.currentTimeMillis())
                .subscribe(System.out::println);

        // add error handling
        Observable.create(subscriber -> {
            try {
                subscriber.onNext(getData());
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .map(data -> data + " --> at " + System.currentTimeMillis())
                .onErrorResumeNext(e -> Observable.just("Fallback Data"))
                .subscribe(System.out::println);

        // prints out numbers 0-10
        Observable.create(subscriber -> {
            int i = 0;
            while (!subscriber.isUnsubscribed()) {
                subscriber.onNext(i++);
            }
        }).take(10).subscribe(System.out::println);

        //Hello World
        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorResumeNext(throwable -> {
            return Observable.just("fallback value");
        }).subscribe(System.out::println);


        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorResumeNext(Observable.just("fallback value"))
                .subscribe(System.out::println);


        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorReturn(throwable -> {
            return "fallback value";
        }).subscribe(System.out::println);


        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).retryWhen(attempts -> {
            return attempts.zipWith(Observable.range(1, 3), (throwable, i) -> i)
                    .flatMap(i -> {
                        System.out.println("delay retry by " + i + " second(s)");
                        return Observable.timer(i, TimeUnit.SECONDS);
                    }).concatWith(Observable.error(new RuntimeException("Exceeded 3 retries")));
        })
                .subscribe(System.out::println, t -> t.printStackTrace());

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }


    private static String getData() {
        return "Got Data!";
    }

}
