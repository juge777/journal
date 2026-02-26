package com.journal.config;

import com.journal.entity.Diary;
import com.journal.entity.User;
import com.journal.repository.DiaryRepository;
import com.journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // 仅用于初始化空数据库，有用户后不再自动创建示例数据
        if (diaryRepository.count() == 0 && userRepository.count() > 0) {
            log.info("Database is empty, seeding sample data for first user...");

            User firstUser = userRepository.findAll().get(0);

            Diary diary1 = new Diary();
            diary1.setUserId(firstUser.getId());
            diary1.setTitle("美好的一天");
            diary1.setContent("今天天气真好，阳光明媚。早上起来去公园跑了一圈，感觉整个人都精神了许多。\n\n下午和朋友一起去喝咖啡，聊了很多有趣的话题。晚上回家做了自己喜欢吃的菜，感觉很满足。\n\n希望明天也能有这样的好心情！");
            diary1.setMood("happy");
            diary1.setWeather("sunny");
            diary1.setCreatedAt(LocalDateTime.now().minusDays(2));
            diary1.setUpdatedAt(LocalDateTime.now().minusDays(2));

            Diary diary2 = new Diary();
            diary2.setUserId(firstUser.getId());
            diary2.setTitle(null);
            diary2.setContent("今天下了一整天的雨，待在家里哪也没去。不过这样也好，难得有时间安静下来看看书，听听音乐。\n\n下午整理了一下房间，发现了很多以前的东西，每一样都有满满的回忆。\n\n雨天虽然让人心情有点低落，但也有一种特别的宁静。");
            diary2.setMood("calm");
            diary2.setWeather("rainy");
            diary2.setCreatedAt(LocalDateTime.now().minusDays(1));
            diary2.setUpdatedAt(LocalDateTime.now().minusDays(1));

            Diary diary3 = new Diary();
            diary3.setUserId(firstUser.getId());
            diary3.setTitle("项目上线啦！");
            diary3.setContent("经过几个月的努力，项目终于上线了！虽然过程很辛苦，加班加点是常态，但是看到成果的那一刻，所有的辛苦都值得了。\n\n团队一起吃了庆祝晚餐，大家都很开心。感谢队友们的支持和帮助。\n\n接下来要继续优化功能，收集用户反馈。加油！");
            diary3.setMood("excited");
            diary3.setWeather("cloudy");
            diary3.setCreatedAt(LocalDateTime.now());
            diary3.setUpdatedAt(LocalDateTime.now());

            diaryRepository.save(diary1);
            diaryRepository.save(diary2);
            diaryRepository.save(diary3);

            log.info("Sample data seeded successfully!");
        }
    }
}
