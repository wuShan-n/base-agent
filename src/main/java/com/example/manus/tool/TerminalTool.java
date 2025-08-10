package com.example.manus.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Component
public class TerminalTool {

    @Tool("执行一个本地终端命令。这可以用来与操作系统交互，例如列出文件 ('ls -l') 或查看当前路径 ('pwd')。出于安全考虑，只允许执行无害的只读命令。")
    public String executeTerminalCommand(@P("要执行的完整终端命令, 例如 'ls -a'") String command) {
        System.out.println(" TerminalTool is called with command: " + command);

        // 安全性检查：简单的黑名单，防止执行危险命令
        String[] blacklistedCommands = {"rm", "sudo", "mv", "mkfs"};
        for (String blacklisted : blacklistedCommands) {
            if (command.trim().startsWith(blacklisted)) {
                return "错误：出于安全考虑，该命令被禁止执行。";
            }
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s+"));
            processBuilder.redirectErrorStream(true); // 将错误流合并到标准输出流

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待命令执行完成，设置一个超时时间（例如15秒）
            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                process.destroy(); // 超时则销毁进程
                return "错误：命令执行超时。";
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                return "命令成功执行，输出结果:\n" + output.toString();
            } else {
                return String.format("命令执行失败，退出码: %d\n输出结果:\n%s", exitCode, output.toString());
            }

        } catch (Exception e) {
            return "执行命令时发生异常: " + e.getMessage();
        }
    }
}
