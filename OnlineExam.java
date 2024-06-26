import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class User implements Serializable
{

    private String username;
    private String password;
    private String role;
    private String name;
    private String email;

    public User(String username,String password,String role,String name,String email)
    {
        this.username=username;
        this.password=password;
        this.role=role;
        this.name=name;
        this.email=email;
    }  
      
    public String getUsername()
    {
        return username;

    }
    public String getRole()
    {
        return role;

    }
     public String getName()
     {
        return name;

     }
     public  String getEmail()
     {
       return email;
     }

     public boolean checkPassword(String password)
     {
        return this.password.equals(password);
     }
     public void setPassword (String password)
     {
        this.password=password;

     }
     public void updateProfile(String name,String email)
     {
        this.name=name;
        this.email=email;
     }

}

class Question implements Serializable
{
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;

    public Question(String questionText, List<String>options,int correctOptionIndex)
    {
        this.questionText=questionText;
        this.options=options;
        this.correctOptionIndex=correctOptionIndex;
    }
    public String getQuestionText()
    {
        return questionText;

    }
    public List<String> getOption()
    {
        return options;
    }
    public boolean isCorrectAnswer(int index)
    {
        return index== correctOptionIndex;
    }
}
class Exam implements Serializable
{
    private String title;
    private List<Question> questions;
    private int duration;

    public Exam(String title,int duration)

    {
        this.title=title;
        this.questions=new ArrayList<>();
        this.duration=duration;
    }

    public String getTitle()
    {
        return title;

    }
    public void addQuestion(Question question)
    {
        questions.add(question);
    }
    public List<Question>getQuestions()
    {
      return questions;

    }
    public int getDuration()
    {
        return duration;
    }
}

class Result implements Serializable
{
    private String username;
    private String examTitle;
    private int score;

    public Result(String username,String examTitle,int score)
    {
        this.username=username;
        this.examTitle=examTitle;
        this.score=score;
    }
    public String getUsername()
    {
        return username;

    }
    public String getExamTitle()
    {
         return examTitle;
    }
    public int getScore()
    {
        return score;
    }
}
class Session 
{
    private User user;
    private Exam exam;
    private ScheduledExecutorService scheduler;
    private boolean submitted;

    /**
     * @param user
     * @param exam
     */
    public Session(User user,Exam exam)
    {
        this.user=user;
        this.exam=exam;
        this.submitted=false;
        this.scheduler=Executors.newScheduledThreadPool(1);
    }
    public void startExam()
    {
        int duration=exam.getDuration();
       scheduler.schedule(() ->{
            if(!submitted)
            {
                System.out.println("Time is up! Auto-submitting the exam.");
                submitExam();
            }

        },duration,TimeUnit.MINUTES);
    }
    private void submitExam() {

        scheduler.shutdown();
        int score=exam.getQuestions().size();
        ExamSystem.saveResult(new Result(user.getUsername(),exam.getTitle(),score));
         submitted=true;

    }
    public void closeSession()
    {
        if(!submitted)
        {
            submitExam();
        }
    }


}
class ExamSystem{
    private static Map<String,User>users;
    private static Map<String,Exam>exams;
    private static List<Result> results;
    private String dataFile;
    private static Map<String ,Session>sessions;

    public ExamSystem(String dataFile)
    {
        this.users = new HashMap<>();
        this.exams = new HashMap<>();
        this.results = new ArrayList<>();
        this.dataFile = dataFile;
        this.sessions = new HashMap<>();
         loadSystem();
    }
    public boolean registerUser(String username, String password, String role, String name, String email) {
        if (!users.containsKey(username)) {
            users.put(username, new User(username, password, role, name, email));
            saveSystem();
            return true;
        }
        return false;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        return null;
    }

    public boolean updateUserProfile(String username, String name, String email) {
        User user = users.get(username);
        if (user != null) {
            user.updateProfile(name, email);
            saveSystem();
            return true;
        }
        return false;
    }

    public boolean updateUserPassword(String username, String oldPassword, String newPassword) {
        User user = users.get(username);
        if (user != null && user.checkPassword(oldPassword)) {
            user.setPassword(newPassword);
            saveSystem();
            return true;
        }
        return false;
    }
    public boolean createExam(String title, int duration) {
        if (!exams.containsKey(title)) {
            exams.put(title, new Exam(title, duration));
            saveSystem();
            return true;
        }
        return false;
    }
    public Exam getExam(String title) {
        return exams.get(title);
    }

    public static void saveResult(Result result) {
        results.add(result);
    }

    public List<Result> getResults() {
        return results;
    }
    private void saveSystem() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(users);
            oos.writeObject(exams);
            oos.writeObject(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

private void loadSystem() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            users = (Map<String, User>) ois.readObject();
            exams = (Map<String, Exam>) ois.readObject();
            results = (List<Result>) ois.readObject();
        } catch (FileNotFoundException e) {
            users = new HashMap<>();
            exams = new HashMap<>();
            results = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void createSession(User user, Exam exam) {
        if (!sessions.containsKey(user.getUsername())) {
            Session session = new Session(user, exam);
            session.startExam();
            sessions.put(user.getUsername(), session);
        }
    }
    public static void closeSession(String username) {
        Session session = sessions.get(username);
        if (session != null) {
            session.closeSession();
            sessions.remove(username);
        }
    }
}



public class OnlineExam {
    public static void main (String args [])
    {
       ExamSystem examSystem = new ExamSystem("exam_system.ser");
        examSystem.registerUser("admin", "adminpass", "admin", "Admin User", "admin@example.com");
        examSystem.registerUser("student", "studentpass", "student", "Student User", "student@example.com");

        User admin = examSystem.loginUser("admin", "adminpass");
        if (admin != null && admin.getRole().equals("admin")) {
            examSystem.createExam("Java Basics", 1); // 1 minute duration for testing
            Exam javaExam = examSystem.getExam("Java Basics");
            javaExam.addQuestion(new Question("What is Java?", Arrays.asList("Programming Language", "Coffee", "Animal"), 0));
            javaExam.addQuestion(new Question("What is JVM?", Arrays.asList("Java Virtual Machine", "JavaScript Version Manager", "Java Vendor Machine"), 0));
        }

        // Student logging in and taking the exam
        User student = examSystem.loginUser("student", "studentpass");
        if (student != null && student.getRole().equals("student")) {
            Exam javaExam = examSystem.getExam("Java Basics");
            ExamSystem.createSession(student, javaExam);

            // Simulate student answering questions
            Scanner scanner = new Scanner(System.in);
            int score = 0;
            for (Question question : javaExam.getQuestions()) {
                System.out.println(question.getQuestionText());
                List<String> options = question.getOption();
                for (int i = 0; i < options.size(); i++) {
                    System.out.println((i + 1) + ". " + options.get(i));
                }
                // Simulate student choosing an answer (1-based index)
                int answer = scanner.nextInt() - 1;
                if (question.isCorrectAnswer(answer)) {
                    score++;
                }
            }

            // Submit the exam manually
            ExamSystem.closeSession(student.getUsername());
        }

        // Update profile and password
        if (student != null) {
            examSystem.updateUserProfile("student", "New Student User", "newstudent@example.com");
            examSystem.updateUserPassword("student", "studentpass", "newstudentpass");
        }

        // Display results
        for (Result result : examSystem.getResults()) {
            System.out.println(result.getUsername() + " scored " + result.getScore() + " on " + result.getExamTitle());
        }

        // Logout session
        if (student != null) {
            ExamSystem.closeSession(student.getUsername());
        }
    }


    }

