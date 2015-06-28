package player.ki;

import board.ChessGame;

import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 * @author stephan
 */
public class StaticEvaluationTest
{
  @Test
	public void testEvaluation()
	{
		String des = "black 0 Rock-white-a1-0 King-white-e1-0 "
												+"Rock-black-a8-0 King-black-e8-0 ";
		ChessGame game = new ChessGame( des );
		
		StaticEvaluation evaluation = new StaticEvaluation();
		assertTrue( evaluation.evaluate(game,true) ==0 );
		assertTrue( evaluation.evaluate(game,false)==0 );
		
		des = "black 0 Rock-white-a1-0 King-white-e1-0 King-black-e8-0";
		game = new ChessGame( des );
		
		assertTrue( evaluation.evaluate(game,true) >0 );
		assertTrue( evaluation.evaluate(game,false)<0 );
	}
}
