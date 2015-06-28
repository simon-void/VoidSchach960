package figures;

import java.util.List;

import image.FigureImage;
import image.FigureImageMock;
import helper.*;
import board.*;
/**
 * @author stephan
 */
class MockFigure extends Figure
{  
	MockFigure( FigureImage figureImage, boolean isWhite,Position position )
	{
		super( figureImage,isWhite,position,(byte)7  );
	}

  MockFigure( boolean isWhite,Position position )
  {
    this( FigureImageMock.defaultInstance, isWhite, position  );
  }
	
	public boolean isReachable(Position to,BasicChessGameInterface game)
	{
		return false;
	}
	
	public void getReachableMoves( BasicChessGameInterface game,List<Move> result )
	{
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game )
	{
		return true;
	}
	
	public int countReachableMoves( BasicChessGameInterface game )
	{
		return 0;
	}
	
	protected String getType()
	{
		return "MockFigure";
	}
}
