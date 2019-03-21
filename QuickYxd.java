package com.zzq.simple.control;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
@SuppressWarnings("all")
class QuickYxd {

    private Map<String,String> requestHeader = new HashMap<>();
    private final String BasicUrl = "ip/englishmet/course/student/";
    private String beginTOAnswerUrl;
    /***
     * 题目以及左右时间的参数
     */
    private LinkedHashMap<String, String> allFuntionParams = new LinkedHashMap<>();
    /***
     *问题和答案
     */
    private LinkedHashMap<String, String> questionAndAnswers = new LinkedHashMap<>();
    /***
     *暂时的保存答案
     */
    private List<String> temporaryAnswers = new ArrayList<>();
    private String username;
    private String password;

    QuickYxd(String username, String password, String urlType) {
        this.username = username;
        this.password = password;
        requestHeader.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        requestHeader.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        requestHeader.put("Accept-Encoding","gzip, deflate");
        requestHeader.put("Accept-Language","zh-CN,zh;q=0.9");
        requestHeader.put("Cache-Control","max-age=0");
        requestHeader.put("Connection","keep-alive");
        requestHeader.put("Cookie",returnCookies());
        requestHeader.put("Host","host");
        requestHeader.put("Referer","ip/englishmet/index.jsp");
        requestHeader.put("Upgrade-Insecure-Requests","1");
        beginTOAnswerUrl = BasicUrl + urlType;
    }

    private static String returnDecorateUrl(HashMap<String, String> allFuntionParams,HashMap<String, String> questionAndAnswers){
        //提交地址
        StringBuilder sb = new StringBuilder();
        sb.append("ip/englishmet/course/student/submitSpecialWork.do?");
        //提交空的参数
        for (String str :allFuntionParams.keySet()) {
            sb.append(str + "=" + allFuntionParams.get(str) + "&");
        }
        for (String str :questionAndAnswers.keySet()) {
            sb.append(str + "=" + questionAndAnswers.get(str)+ "&");
        }
        return sb.toString().substring(0,sb.toString().length()-1);
    }

    private String enterUrl(String webUrl){
        Connection conn = Jsoup.connect(webUrl);
        for (String str :requestHeader.keySet()) {
            conn.header(str,requestHeader.get(str));
        }
        conn.method(Connection.Method.GET).followRedirects(false).ignoreContentType(true);
        Connection.Response response = null;
        try {
            response = conn.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response != null ? response.body() : null;
    }

    private String returnCookies() {
        try {
            Connection conn = Jsoup.connect("ip/englishmet/j_security_check?j_username="+username+
                    "&j_password="+password).method(Connection.Method.GET).followRedirects(false);
            Connection.Response response = conn.execute();
            Map<String, String> getCookies = response.cookies();
            String loginCookie = getCookies.toString();
            return loginCookie.substring(loginCookie.indexOf("{")+1, loginCookie.lastIndexOf("}"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private boolean containJudge(String totalWords,String keyWord){
        return totalWords.contains(keyWord);
    }
    private void splitUrl(String webUrl){
        String[] string = webUrl.split("\\?");
        for (String str :string) {
            if(str.contains("=")){
                String[] split = str.split("&");
                for (String s :split) {
                    String[] last = s.split("=");
                    allFuntionParams.put(last[0],last[1]);
                    System.out.println("添加题目参数"+ last[0] + "=" + last[1] + "成功！");
                }
            }
        }
    }
    private void addToSetFrame(Elements webOutput){
        for (Element element:webOutput) {
            questionAndAnswers.put(element.attr("name"),"");
            System.out.println("添加" + element.attr("name") + "成功！");
        }
    }
    private void saveToSetFrame(String webUrl){
        String regex = "[\\. | ][\\u0391-\\uFFE5 | ]+";
        List<String> b = Jsoup.parse(webUrl).select("[style=color:gray;padding-bottom:10px]").eachText();
        for (int i = 0, j = 0; i < b.size(); i++,j++) {
            String[] directAnswer = b.get(i).split(":");
            Pattern compile = Pattern.compile(regex);
            directAnswer[2] = compile.matcher(directAnswer[2]).replaceAll(" ").trim();
            temporaryAnswers.add(directAnswer[2]);
        }
        Iterator<Map.Entry<String, String>> iteratorMap = questionAndAnswers.entrySet().iterator();int i = 0;
        while (iteratorMap.hasNext() && i < temporaryAnswers.size()){
            iteratorMap.next().setValue(temporaryAnswers.get(i));
            i++;
        }
    }
    private void printTip(String totalWords,String keyWord,String sucessWords,String failWords){
        if (containJudge(totalWords,keyWord)){
            System.out.println(sucessWords);
        }else {
            System.out.println(failWords);
        }
    }

    public void beginAnswer(){
        String loginAndAnswer = enterUrl(beginTOAnswerUrl);
        String tips = "今天的任务已完成.";
        assert loginAndAnswer != null;
        if(containJudge(loginAndAnswer,tips))
        {
            throw new RuntimeException("已经刷完了，不需要在刷了，老铁");
        }
        printTip(loginAndAnswer,"数学171","登录成功...\n正在搜寻正确答案中...","登录失败！请联系q256598791解决");
        String webUrl = BasicUrl + Jsoup.parse(loginAndAnswer).select(".dotted_box a").attr("href");
        //分割URL，获得参数
        splitUrl(webUrl);
        //以questionSave开头的
        String questionSave = enterUrl(webUrl);
        assert questionSave != null;
        Elements inputSet = Jsoup.parse(questionSave).getElementsByTag("input").select("[name^=questionSave]");
        if(inputSet.size() == 0)
        {
            inputSet = Jsoup.parse(questionSave).getElementsByTag("select").select("[name^=questionSave]");
        }
        addToSetFrame(inputSet);
    }
    public void getCorrectAnswers(){
        String blankSubmit = enterUrl(returnDecorateUrl(allFuntionParams,questionAndAnswers)+"&su4bmitBtn=");
        printTip(blankSubmit,"未完成,指定的80%正确率未达到","提交成功\n即将获得答案！请稍后...","抓取网页失败,请联系q256598791");
        String beginUrl = enterUrl(beginTOAnswerUrl);
        assert beginUrl != null;
        String recordURl = "ip/englishmet/course/student/" + Jsoup.parse(beginUrl).select(".margin_b a").attr("href");
        //刚刚答得那道题得URL
        String recordUrlString = enterUrl(recordURl);
        assert recordUrlString != null;
        String select = Jsoup.parse(recordUrlString).select(".myTable tr a").get(0).attr("href");
        saveToSetFrame(enterUrl("ip/englishmet/course/student/" + select));
        for (Map.Entry<String,String> entry:questionAndAnswers.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }
    public void submitCorrectAnswers(){
        String lastQuestionsAndAnswers = returnDecorateUrl(allFuntionParams,questionAndAnswers);
        printTip(enterUrl(lastQuestionsAndAnswers),"alert(\"完成!\");","刷成功了，快去雅信达看看吧！\n地址是：ip/englishmet/index.jsp\ntip：选中-》右键 即可复制，然后粘贴到浏览器，就可以查看","由于某些原因，刷入失败，正在重新刷，请稍后....");
    }
}
