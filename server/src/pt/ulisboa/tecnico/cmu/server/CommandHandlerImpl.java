package pt.ulisboa.tecnico.cmu.server;

import pt.ulisboa.tecnico.cmu.communication.command.*;
import pt.ulisboa.tecnico.cmu.data.Quiz;
import pt.ulisboa.tecnico.cmu.data.SessionID;
import pt.ulisboa.tecnico.cmu.data.User;
import pt.ulisboa.tecnico.cmu.data.Question;
import pt.ulisboa.tecnico.cmu.communication.response.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class CommandHandlerImpl implements CommandHandler {

	@Override
	public Response handle(HelloCommand hc) {
		System.out.println("Received: " + hc.getMessage());
		return new HelloResponse("Hi from Server!");
	}

	@Override
	public Response handle(TicketCommand tc) {
		System.out.println("Este é o bilhete recebido " + tc.getTicketCode());
		if (Server.validTicket(tc.getTicketCode())) {
			for (User user : Server.getUsers()) {
				if (user.getTicketCode().equals(tc.getTicketCode())) {
					//ticket code already used so can login
                    String sessionID;
                    while (true){
                        sessionID = generateSessionID(user.getUserID());
                        if (sessionID != null){
                            break;
                        }
                    }
					return new TicketResponse("OK", user.getUserID(), getMonuments(user.getUserID()), sessionID);
				}
			}
			//ticket never used (NU), so need to create an account
			return new TicketResponse("NU", null, null,  null);
		}
		return new TicketResponse("NOK", null, null, null);
	}

	@Override
	public Response handle(SignUpCommand suc) {
		System.out.println("Bilhete recebido " + suc.getTicketCode() + " user: " + suc.getUserID());
		for (User user : Server.getUsers()) {
			if (user.getUserID().equals(suc.getUserID())) {
				//ticket userID already used
				return new SignUpResponse("NOK", null, null, null);
			}
		}
		User user = new User(suc.getUserID(), suc.getTicketCode(), 0);
		Server.getUsers().add(user);
        String sessionID;
		while (true){
            sessionID = generateSessionID(user.getUserID());
            if (sessionID != null){
                break;
            }
        }
		return new SignUpResponse("OK", user.getUserID(), getMonuments(user.getUserID()), sessionID);
	}

	@Override
	public Response handle(GetQuizCommand gqc) {
		System.out.println("Quiz " + gqc.getMonumentName());
		for (Quiz quiz : Server.getQuizzes()) {
			if (quiz.getMonumentName().equals(gqc.getMonumentName())) {
				return new GetQuizResponse(quiz);
			}
		}
		return new GetQuizResponse(null);
	}

	@Override
	public Response handle(GetRankingCommand grc) {
		Map<String, Integer> unsortRanking = new HashMap<>();
		for (User user : Server.getUsers()) {
			unsortRanking.put(user.getUserID(), user.getScore());
		}
		return new GetRankingResponse(Server.sortByScore(unsortRanking));
	}

	@Override
	public Response handle(SubmitQuizCommand sqc) {
		System.out.println("Recebi as respostas ao quiz " + sqc.getAnswers().get(0) + sqc.getAnswers().get(1) + sqc.getAnswers().get(2) );
		List<Question> questions = Server.getQuiz(sqc.getQuizName());
		int cnt = 0;
		int score = 0;
		if(questions != null){
			for (Question question: questions) {
				if(sqc.getAnswers().get(cnt).equals(question.getSolution())){
					//need to increase the user score by one
					score++;
				}
				cnt++;
			}
			System.out.println("Score " + score);
			Server.updateUserScore(sqc.getUserID(), score, sqc.getQuizName());
			return new SubmitQuizResponse("OK");
		}
		return new SubmitQuizResponse("NOK");
	}

	private List<String> getMonuments(String userID) {
		List<String> monumentsNames = new ArrayList<>();
		for (Quiz quiz : Server.getQuizzes()) {
		    if(quiz.getUserAnswers().containsKey(userID)){
                monumentsNames.add(quiz.getMonumentName() + "|T");
            }
            else {
                monumentsNames.add(quiz.getMonumentName() + "|F");
            }
		}
		return monumentsNames;
	}

	private String generateSessionID(String userID) {
	    try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            //generate a random number
            String sessionID = Integer.toString(secureRandom.nextInt());
            System.out.println("SESSION ID: " + sessionID);
            if (notUSedSessionID(sessionID, userID)){
                Date time = new Date();
                System.out.println("__________________ " + time.getTime());
                Server.updateSessionID(userID, new SessionID(sessionID, new Date()));
                return sessionID;
            }
        } catch (NoSuchAlgorithmException nae){
	        nae.getMessage();
        }
        return null;
    }

    private boolean notUSedSessionID(String sessionID, String userID) {
        //return !Server.getSessionID().containsValue(sessionID);
        if (Server.getSessionID().containsKey(userID)){
            SessionID session = Server.getSessionID().get(userID);
            return !session.getSessionID().equals(sessionID);
        }
        return true;
    }

    private boolean validateSessionID(String sessionID, String userID) {
	    if (Server.getSessionID().containsKey(userID)){
            SessionID session = Server.getSessionID().get(userID);
            if (session.getSessionID().equals(sessionID)){
                Date last = session.getGeneratedTime();
                Date now = new Date();
                long diff = Math.abs(now.getTime() - last.getTime());
                System.out.println(diff);
                // 300 equals 5 minutes of a session
                if (diff < 300) {
                    session.setGeneratedTime(now);
                    Server.updateSessionID(userID, session);
                    return true;
                }
                else {
                    Server.removeSession(userID);
                    return false;
                }
            }
        }
        return false;
    }

}
