package com.mszq.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.mszq.demo.bean.User;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SlbController {

    @Value("${message}")
    private String message;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @GetMapping("/take/{method}")
    public void takeSecondRequest(@PathVariable("method") String method){
        long startTime = System.currentTimeMillis();
        String url = "http://spring-boot-svc:8080/" + method;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;

            if (response.isSuccessful()) {
                long endTime = System.currentTimeMillis();
                log.info("take half second: {} ms",(endTime - startTime));
            }
        } catch (IOException e) {
            log.error("查询是否存在网关失败,OKHttp请求异常",e);
        }
    }

    @GetMapping("/sleepHalfSecond")
    public void sleepHalfSecond() throws InterruptedException {
        Thread.sleep(500);
        log.info("sleepHalfSecond");
    }

    @GetMapping("/sleepOneSecond")
    public void sleepOneSecond() throws InterruptedException {
        Thread.sleep(1000);
        log.info("sleepOneSecond");
    }

    @GetMapping("/sleepTwoSecond")
    public void sleepTwoSeconds() throws InterruptedException {
        Thread.sleep(2000);
        log.info("sleepTwoSeconds");
    }

    @GetMapping("/take/cpu/{time}")
    public void takeSecondCpu(@PathVariable("time") Long time){
        long startTime = System.currentTimeMillis();

        // 调用耗时操作
        performExpensiveOperation(startTime,time);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("操作耗时: {} 毫秒",duration);
    }
    public static void performExpensiveOperation(Long startTime, Long time) {
        long count = 0;
        // 尽可能进行多的计算操作来消耗时间
        for (long i = 2; i < 1_000_000_000_000_000_000L; i++) {
            if (isPrime(i)) {
                count++;
            }
            // 检查是否已经经过了时间
            if (System.currentTimeMillis() - startTime >= time) {
                break;
            }
        }
        log.info("找到的质数个数: {}",count);
    }

    // 检查一个数是否为质数

    public static boolean isPrime(long num) {
        if (num < 2) return false;
        for (long i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) return false;
        }
        return true;
    }

    @GetMapping("/createFile")
    public void createFile(){
        // 定义文件路径
        String directoryPath = "/opt/demo";
        String filePath = directoryPath + "/example.txt";

        // 创建目录对象
        File directory = new File(directoryPath);
        // 如果目录不存在，则创建目录
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("目录创建成功: " + directoryPath);
            } else {
                System.out.println("目录创建失败: " + directoryPath);
                return;
            }
        }

        // 创建文件对象
        File file = new File(filePath);
        FileWriter writer = null;
        try {
            // 创建新文件
            if (file.createNewFile()) {
                System.out.println("文件创建成功: " + filePath);
            } else {
                System.out.println("文件已存在: " + filePath);
            }

            // 写入一些内容到文件
            writer = new FileWriter(file);
            writer.write("Hello, this is a test file created by Java.");
            System.out.println("文件写入成功: " + filePath);
        } catch (IOException e) {
            System.err.println("文件操作失败: " + e.getMessage());
        } finally {
            // 关闭文件写入流
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("文件关闭失败: " + e.getMessage());
                }
            }
        }
    }

    @GetMapping("/get")
    public void get(){
        RuntimeException runtimeException = new RuntimeException("异常日志输出");
        //log.info("负载均衡测试{}:{}",uuid,count.incrementAndGet());
        log.info("异常日志",runtimeException);
    }

    @GetMapping("/getSlbInfo")
    public String getSlbInfo(){
        log.info("getSlbInfo");
        return "xxx-abc";
    }

    @GetMapping("/getMessage")
    public String getMessage(){
        log.info("getMessage:{}",message);
        return message;
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        // 创建用户列表
        List<User> users = new ArrayList<>();
        users.add(new User("袁杰", 27));
        users.add(new User("Bob", 25));
        users.add(new User("Charlie", 35));

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("users", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 使用 EasyExcel 写入数据到响应流
        EasyExcel.write(response.getOutputStream(), User.class).sheet("Users").doWrite(users);
    }
}
