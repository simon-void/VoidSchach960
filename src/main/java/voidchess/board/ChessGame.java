package voidchess.board;

import voidchess.figures.*;
import voidchess.helper.*;
import voidchess.player.ki.SimplePruner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author stephan
 */
public class ChessGame implements ChessGameInterface, LastMoveProvider {
    private SimpleChessBoardInterface game;
    private FigureFactory figureFactory;
    private boolean whiteTurn;
    private MementoStack mementoStack;
    private ExtendedMoveStack extendedMoveStack;
    private NumberStack numberStack;
    private int numberOfMovesWithoutHit;
    private int figureCount;
    private boolean hasHitFigure;
    private ChessGameSupervisor supervisor;
    private boolean isStandardGame = false;

    /**
     * der normale Konstruktor, der von außerhalb verwendet werden sollte
     */
    public ChessGame(ChessGameSupervisor supervisor) {
        hasHitFigure = false;
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();
        game = new SimpleArrayBoard(this);

        initGame();
    }

    /**
     * für CopyConstructor
     */
    public ChessGame(ChessGame other, String desc) {
        hasHitFigure = other.hasHitFigure;
        supervisor = ChessGameSupervisorDummy.INSTANCE;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack(other.mementoStack);
        extendedMoveStack = new ExtendedMoveStack(other.extendedMoveStack);
        numberStack = new NumberStack(other.numberStack);
        game = new SimpleArrayBoard(desc, this);

        whiteTurn = other.whiteTurn;
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit;
        figureCount = other.figureCount;
    }

    /**
     * für JUnit-TestKlassen
     */
    public ChessGame(String game_description) {
        this(ChessGameSupervisorDummy.INSTANCE, game_description);
    }

    /**
     * für JUnit-TestKlassen
     */
    public ChessGame(int initialPosition) {
        this(ChessGameSupervisorDummy.INSTANCE, initialPosition);
    }

    /**
     * für JUnit-TestKlassen: Standardaufstellung
     */
    public ChessGame() {
        this(518);
    }

    /**
     * wird nur implizit für JUnit-tests verwendet
     */
    private ChessGame(ChessGameSupervisor supervisor, String game_description) {
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();

        StringTokenizer st = new StringTokenizer(game_description, " ", false);
        whiteTurn = st.nextToken().equals("white");
        numberOfMovesWithoutHit = Integer.parseInt(st.nextToken());
        for (int i = 0; i < numberOfMovesWithoutHit; i++) numberStack.noFigureHit();

        figureCount = 0;
        while (st.hasMoreTokens()) {
            figureCount++;
            st.nextToken();
        }

        game = new SimpleArrayBoard(this);
        game.init(game_description);

        memorizeGame();
        hasHitFigure = numberOfMovesWithoutHit == 0;
    }

    /**
     * wird nur implizit für JUnit-tests verwendet
     */
    private ChessGame(ChessGameSupervisor supervisor,
                      int initialPosition) {
        this.supervisor = supervisor;
        figureFactory = new FigureFactory();
        mementoStack = new MementoStack();
        extendedMoveStack = new ExtendedMoveStack();
        numberStack = new NumberStack();

        whiteTurn = true;
        hasHitFigure = false;
        numberOfMovesWithoutHit = 0;
        figureCount = 32;

        game = new SimpleArrayBoard(this);
        game.init(initialPosition);

        memorizeGame();
    }

    @Override
    public void useSupervisor(ChessGameSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public ChessGameSupervisor suspendInteractiveSupervisor() {
        ChessGameSupervisor normalSupervisor = supervisor;
        supervisor = ChessGameSupervisorDummy.INSTANCE;
        return normalSupervisor;
    }

    @Override
    public ExtendedMove getLastMove() {
        if (extendedMoveStack.isEmpty()) return null;
        return extendedMoveStack.topExtendedMove();
    }

    @Override
    public Position getKingPosition(boolean whiteKing) {
        return game.getKingPosition(whiteKing);
    }

    @Override
    public boolean isFreeArea(Position pos) {
        return game.isFreeArea(pos);
    }

    @Override
    public Figure getFigure(Position pos) {
        return game.getFigure(pos);
    }

    @Override
    public BoardContent getContent(Position pos) {
        return BoardContent.get(game.getFigure(pos));
    }

    private void setFigure(Position pos, Figure figure) {
        if(figure==null) {
            game.clearFigure(pos);
        } else {
            game.setFigure(pos, figure);
        }
    }

    @Override
    public List<Figure> getFigures() {
        return game.getFigures();
    }

    @Override
    public boolean isSelectable(Position pos, boolean whitePlayer) {
        if (isFreeArea(pos)) return false;
        Figure figure = getFigure(pos);
        return figure.isWhite() == whitePlayer && figure.isSelectable(game);
    }

    @Override
    public boolean isMovable(Position from, Position to, boolean whitePlayer) {
        if (isFreeArea(from)) return false;
        Figure figure = getFigure(from);
        return figure.isWhite() == whitePlayer && figure.isMovable(to, game);
    }

    @Override
    public int countFigures() {
        return figureCount;
    }

    @Override
    public MoveResult move(Move move) {
        assert !isFreeArea(move.from)
                : "the move moves a null value:" + move.toString();
        assert (getFigure(move.from).isWhite() == whiteTurn)
                : "figure to be moved has wrong color";

        Rook castlingRook = extractCastlingRook(move);
        //im Fall der Castling wird der Zug jetzt so umgebogen,
        //das move.to dem Zielfeld des Königs entspricht
        //und nicht dem Feld des Castlingturms
        if (castlingRook != null) {
            final int row = move.to.row;
            final int column = move.to.column - move.from.column > 0 ? 6 : 2;
            move = Move.get(move.from, Position.get(row, column));
        }

        Pawn hitPawn = handleEnpasent(move);
        Figure hitFigure = moveFigure(move);

        informFiguresOfMove(move);
        reinsertCastlingRook(castlingRook, move.to);
        boolean pawnTransformed = handlePawnTransformation(move);

        memorizeGame();
        memorizeMove(move, !whiteTurn, pawnTransformed, hitPawn, castlingRook, hitFigure);

        return isEnd();
    }

    private Figure moveFigure(Move move) {
        final boolean toNotEqualsFrom = move.to.notEqualsPosition(move.from);//für manche Schach960castlingn true
        hasHitFigure = !isFreeArea(move.to) && toNotEqualsFrom;  //Enpasent wird nicht beachtet
        Figure fromFigure = getFigure(move.from);

        if (hasHitFigure) {
            numberStack.figureHit();
            numberOfMovesWithoutHit = 0;
            figureCount--;
        } else {
            numberStack.noFigureHit();
            numberOfMovesWithoutHit++;
        }

        Figure hitFigure = null;
        if (toNotEqualsFrom) {
            hitFigure = getFigure(move.to);
            setFigure(move.to, fromFigure);
            setFigure(move.from, null);
        }

        whiteTurn = !whiteTurn;

        return hitFigure;
    }

    @Override
    public boolean hasHitFigure() {
        return hasHitFigure;
    }

    private Pawn handleEnpasent(Move move) {
        if (getFigure(move.from).isPawn()
                && move.from.column != move.to.column
                && isFreeArea(move.to)
        ) {
            Position pawnToBeHit = Position.get(move.from.row, move.to.column);
            Pawn pawn = (Pawn) getFigure(pawnToBeHit);
            setFigure(pawnToBeHit, null);
            figureCount--;
            numberOfMovesWithoutHit = -1;            //die Variable wird dann von move Figure auf 0 gesetzt
            return pawn;
        }
        return null;
    }

    private Rook extractCastlingRook(Move move) {
        final Figure movingFigure = getFigure(move.from);
        if (!(movingFigure.isKing())) return null;

        final Figure castlingRook = getFigure(move.to);
        if (castlingRook != null && castlingRook.isWhite() == movingFigure.isWhite()) {
            setFigure(move.to, null);    //der Turm wird kurzfristig vom Brett genommen
            ((King) movingFigure).performCastling();
            return (Rook) castlingRook;
        }
        return null;
    }

    private void reinsertCastlingRook(Rook castlingRook, Position moveTo) {
        if (castlingRook != null) {
            Position RookFrom = castlingRook.getPosition();
            Position RookTo = moveTo.column == 6 ?
                    Position.get(moveTo.row, 5) :
                    Position.get(moveTo.row, 3);
            castlingRook.figureMoved(Move.get(RookFrom, RookTo));
            setFigure(RookTo, castlingRook);
        }
    }

    private boolean handlePawnTransformation(Move move) {
        if (getFigure(move.to).isPawn()) {
            if (move.to.row == 0 || move.to.row == 7) {
                PawnPromotion figure = supervisor.askForPawnChange(move.to);
                boolean isWhite = move.to.row == 7;
                Figure newFigure;
                switch (figure) {
                    case QUEEN:
                        newFigure = figureFactory.getQueen(move.to, isWhite);
                        break;
                    case ROOK:
                        newFigure = figureFactory.getRook(move.to, isWhite);
                        break;
                    case KNIGHT:
                        newFigure = figureFactory.getKnight(move.to, isWhite);
                        break;
                    case BISHOP:
                        newFigure = figureFactory.getBishop(move.to, isWhite);
                        break;
                    default:
                        throw new NullPointerException("invalide pawn-transformation-string:" + figure);
                }
                setFigure(move.to, newFigure);
                return true;
            }
        }
        return false;
    }

    private MoveResult isEnd() {
        if (noMovesLeft(whiteTurn)) {
            if (isCheck(whiteTurn)) {
                return MoveResult.CHECKMATE;
            } else {
                return MoveResult.STALEMATE;
            }
        }
        if (isDrawBecauseOfLowMaterial()) {
            return MoveResult.DRAW;
        }
        if (isDrawBecauseOfThreeTimesSamePosition()) {
            return MoveResult.THREE_TIMES_SAME_POSITION;
        }
        if (numberOfMovesWithoutHit == 100) {
            return MoveResult.FIFTY_MOVES_NO_HIT;
        }
        return MoveResult.NO_END;
    }

    private void informFiguresOfMove(Move move) {
        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            figure.figureMoved(move);
        }
    }

    @Override
    public void undo() {
        whiteTurn = !whiteTurn;
        numberOfMovesWithoutHit = numberStack.undo();
        mementoStack.popMemento();

        ExtendedMove lastExtMove = extendedMoveStack.popExtendedMove();
        Move lastMove = lastExtMove.getMove();
        final boolean wasCastling = lastExtMove.isCastling();
        Figure activeFigure = getFigure(lastMove.to);
        setFigure(lastMove.from, activeFigure);
        if (!wasCastling || lastMove.from.notEqualsPosition(lastMove.to)) {
            setFigure(lastMove.to, lastExtMove.getFigureTaken());
        }
        activeFigure.undoMove(lastMove.from);

        if (lastExtMove.wasFigureTaken()) {
            figureCount++;
        }

        if (wasCastling) undoCastling(lastExtMove);
        if (lastExtMove.isEnpassent()) undoEnpassent(lastExtMove);
        if (lastExtMove.isPawnTransformation()) undoPawnTransformation(lastExtMove);
        rebuildPawnEnpassentCapability();
    }

    private void undoCastling(ExtendedMove lastExtMove) {
        Rook rook = (Rook) lastExtMove.getEnpassentPawnOrCastlingRook();
        Position RookStartPos = rook.getInitialPosition();
        Position RookCurrentPos = rook.getPosition();

        setFigure(RookStartPos, rook);
        if (RookStartPos.notEqualsPosition(RookCurrentPos) && lastExtMove.getMove().from.notEqualsPosition(RookCurrentPos)) {
            setFigure(RookCurrentPos, null);
        }
        rook.undoMove(RookStartPos);
    }

    private void undoEnpassent(ExtendedMove lastExtMove) {
        Pawn hitPawn = (Pawn) lastExtMove.getEnpassentPawnOrCastlingRook();
        Position pawnPos = Position.get(lastExtMove.getMove().from.row, lastExtMove.getMove().to.column);
        setFigure(pawnPos, hitPawn);
        hitPawn.setCanBeHitByEnpasent();
    }

    private void undoPawnTransformation(ExtendedMove lastExtMove) {
        Position pawnPos = lastExtMove.getMove().from;
        Figure pawn = figureFactory.getPawn(pawnPos, lastExtMove.getColorOfMove());
        setFigure(pawnPos, pawn);
    }

    private void rebuildPawnEnpassentCapability() {
        if (extendedMoveStack.isEmpty()) return;

        ExtendedMove newLatestMove = extendedMoveStack.topExtendedMove();
        Figure figure = getFigure(newLatestMove.getMove().to);
        if (figure.isPawn() &&
                Math.abs(newLatestMove.getMove().from.row - newLatestMove.getMove().to.row) == 2) {
            ((Pawn) figure).setCanBeHitByEnpasent();
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(512);

        if (whiteTurn) buffer.append("white ");
        else buffer.append("black ");

        buffer.append(numberOfMovesWithoutHit);
        buffer.append(" ");
        buffer.append(game.toString());

        return buffer.toString();
    }

    public String getHistory() {
        return getHistory(4);
    }

    public String getHistory(int numberOfHalvMoves) {
        return extendedMoveStack.getLatestMoves(numberOfHalvMoves);
    }

    @Override
    public String getCompleteHistory() {
        final int numberOfMovesSaved = extendedMoveStack.size();
        return extendedMoveStack.getLatestMoves(numberOfMovesSaved);
    }

    private void initGame() {
        initGame(518);    //normale Schachposition
    }

    @Override
    public void initGame(int chess960) {
        isStandardGame = chess960 == 518;
        whiteTurn = true;
        numberOfMovesWithoutHit = 0;
        figureCount = 32;
        mementoStack.clear();
        extendedMoveStack.clear();
        numberStack.init();

        game.init(chess960);

        memorizeGame();
    }

    public boolean equals(ChessGame other) {
        if (whiteTurn != other.whiteTurn) return false;

        for (int index = 0; index < 64; index++) {
            Position pos = Position.byIndex(index);
            BoardContent content = getContent(pos);
            BoardContent otherContent = other.getContent(pos);
            if (content.isFreeArea() != otherContent.isFreeArea()) return false;
            if (!content.isFreeArea()) {
                Figure figure1 = content.getFigure();
                Figure figure2 = otherContent.getFigure();
                if (!figure1.equals(figure2)) return false;
            }
        }

        return true;
    }

    public boolean isStandardGame() {
        return isStandardGame;
    }

    private boolean noMovesLeft(boolean caseWhite) {
        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == caseWhite &&
                    figure.isSelectable(game)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDrawBecauseOfLowMaterial() {
        int numberOfWhiteBishops = 0;
        int numberOfBlackBishops = 0;
        int numberOfWhiteKnights = 0;
        int numberOfBlackKnights = 0;

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isPawn()
                    || figure.isRook()
                    || figure.isQueen()) {
                return false;
            } else if (figure.isBishop()) {
                if (figure.isWhite()) numberOfWhiteBishops++;
                else numberOfBlackBishops++;
            } else if (figure.isKnight()) {
                if (figure.isWhite()) numberOfWhiteKnights++;
                else numberOfBlackKnights++;
            }
        }

        if (numberOfWhiteBishops > 1 || numberOfBlackBishops > 1) {
            return false;
        }
        if (numberOfWhiteKnights > 2 || numberOfBlackKnights > 2) {
            return false;
        }
        if (numberOfWhiteBishops == 1 && numberOfWhiteKnights > 0) {
            return false;
        }
        if (numberOfBlackBishops == 1 && numberOfBlackKnights > 0) {
            return false;
        }
        return true;
    }

    private boolean isDrawBecauseOfThreeTimesSamePosition() {
        int occurencesOfMemento = mementoStack.countOccurencesOfLastMemento();
        return occurencesOfMemento >= 3;
    }

    @Override
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    @Override
    public boolean isCheck(boolean isWhiteInCheck) {
        return CheckSearch.INSTANCE.isCheck(game, game.getKingPosition(isWhiteInCheck));
    }

    @Override
    public void getPossibleMoves(List<Move> possibleMoves) {
        final Position kingPos = game.getKingPosition(whiteTurn);

        getFigure(kingPos).getPossibleMoves(game, possibleMoves);

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == whiteTurn && !(figure.isKing())) {
                figure.getPossibleMoves(game, possibleMoves);
            }
        }
    }

    @Override
    public int countReachableMoves(boolean forWhite) {
        int count = 0;

        final List<Figure> figures = game.getFigures();
        for (Figure figure : figures) {
            if (figure.isWhite() == forWhite) {
                count += figure.countReachableMoves(game);
            }
        }

        return count;
    }

    @Override
    public List<ChessGameInterface> copyGame(int neededInstances) {
        final List<ChessGameInterface> gameInstances = new ArrayList<ChessGameInterface>(neededInstances);
        gameInstances.add(this);

        if (neededInstances > 1) {
            final String gameDes = toString();
            for (int i = 1; i < neededInstances; i++) {
                ChessGame copy = new ChessGame(this, gameDes);
                gameInstances.add(copy);
            }
        }
        return gameInstances;
    }

    private void memorizeGame() {
        mementoStack.putMemento(new Memento(game, whiteTurn));
    }

    private void memorizeMove(Move move,
                              boolean whiteMove,
                              boolean pawnTransformed,
                              Pawn hitPawn,
                              Rook castlingRook,
                              Figure hitFigure) {
        boolean hitsEnpassent = hitPawn != null;
        boolean isCastling = castlingRook != null;
        Figure castlingRookOrEnpassentPawn = hitPawn;
        if (isCastling) {
            castlingRookOrEnpassentPawn = castlingRook;
        }
        ExtendedMove extendedMove = new ExtendedMove(
                move,
                hitFigure,
                castlingRookOrEnpassentPawn,
                whiteMove,
                isCastling,
                hitsEnpassent,
                pawnTransformed);
        extendedMoveStack.putExtendedMove(extendedMove);
    }

    public ExtendedMove getLastExtendedMove() {
        return extendedMoveStack.topExtendedMove();
    }

    private class Memento {
        final private int figureCount;
        final private boolean isWhite;
        final private long[] compressedBoard;

        private Memento(BasicChessGameInterface game, boolean isWhite) {
            int count = 0;
            int[] board = new int[64];
            for (int index = 0; index < 64; index++) {
                Figure figure = game.getFigure(Position.byIndex(index));
                if (figure != null) {
                    board[index] = figure.getTypeInfo();
                    count++;
                }
            }

            // compress the board by exploiting that typeInfo is smaller than 16
            // and therefore only 4 bits are needed -> pack 15 typeInfos into 1 long
            compressedBoard = new long[]{
                    compressBoardSlicesToLong(board, 0, 15),
                    compressBoardSlicesToLong(board, 15, 30),
                    compressBoardSlicesToLong(board, 30, 45),
                    compressBoardSlicesToLong(board, 45, 60),
                    compressBoardSlicesToLong(board, 60, 64)
            };

            this.isWhite = isWhite;
            figureCount = count;
        }

        private boolean hasDifferentNumberOfFiguresAs(Memento other) {
            return figureCount != other.figureCount;
        }

        private boolean equals(Memento other) {
            return isWhite == other.isWhite && Arrays.equals(compressedBoard, other.compressedBoard);
        }

        private long compressBoardSlicesToLong(int[] board, int startIndex, int endIndex) {
            assert endIndex-startIndex < 16;

            final int endIndexMinusOne = endIndex-1;
            long compressedValue = 0;
            for(int i=startIndex; i<endIndexMinusOne; i++ ) {
                assert board[i]>=0 && board[i]<16; // board[i] (=figure==null?0:figure.typeInfo) out of Bounds, it has to fit into 4 bits with 0->no figure!
                compressedValue+=board[i];
                compressedValue <<=4;
            }
            compressedValue+=board[endIndexMinusOne];
            return compressedValue;
        }
    }

    private class MementoStack {
        private Memento[] mementoArray;
        private int index;

        MementoStack() {
            mementoArray = new Memento[200];
            index = 0;
        }

        //Copy-Constructor
        MementoStack(MementoStack other) {
            mementoArray = new Memento[other.mementoArray.length];
            System.arraycopy(other.mementoArray, 0, mementoArray, 0, mementoArray.length);
            index = other.index;
        }

        void putMemento(Memento memento) {
            ensureCapacity();
            mementoArray[index++] = memento;
        }

        void popMemento() {
            if (index > 0) index--;
        }

        int countOccurencesOfLastMemento() {
            assert index > 0;
            int count = 1;

            Memento lastMemento = mementoArray[index - 1];
            for (int i = index - 3; i >= 0; i = i - 2) {
                Memento previousMemento = mementoArray[i];
                if (previousMemento.hasDifferentNumberOfFiguresAs(lastMemento)) {
                    break;
                }
                if (lastMemento.equals(previousMemento)) {
                    count++;
                }
            }

            return count;
        }

        void clear() {
            index = 0;
        }

        private void ensureCapacity() {
            if (index == mementoArray.length) {
                Memento[] newMementoArray = new Memento[mementoArray.length * 2];
                System.arraycopy(mementoArray, 0, newMementoArray, 0, index);
                mementoArray = newMementoArray;
            }
        }
    }

    private class ExtendedMoveStack {
        private ExtendedMove[] extendedMoveArray;
        private int index;

        ExtendedMoveStack() {
            extendedMoveArray = new ExtendedMove[100];
            index = 0;
        }

        ExtendedMoveStack(ExtendedMoveStack other) {
            extendedMoveArray = new ExtendedMove[other.index + SimplePruner.MAX_SEARCH_DEPTH];
            System.arraycopy(other.extendedMoveArray, 0, extendedMoveArray, 0, extendedMoveArray.length);
            index = other.index;
        }

        void putExtendedMove(ExtendedMove extendedMove) {
            ensureCapacity();
            extendedMoveArray[index++] = extendedMove;
        }

        ExtendedMove popExtendedMove() {
            assert index > 0;
            return extendedMoveArray[--index];
        }

        ExtendedMove topExtendedMove() {
            assert index > 0;
            return extendedMoveArray[index - 1];
        }

        boolean isEmpty() {
            return index == 0;
        }

        void clear() {
            index = 0;
        }

        int size() {
            return index;
        }

        String getLatestMoves(int count) {
            assert count > 0;

            final int MIN_INDEX = Math.max(0, index - count);
            StringBuilder sb = new StringBuilder(24);

            for (int i = MIN_INDEX; i < index; i++) {
                sb.append(extendedMoveArray[i].toString());
                sb.append(",");
            }
            final int deleteCharAt = sb.length() - 1;
            if (deleteCharAt < 0) {
                return "";
            }
            sb.deleteCharAt(deleteCharAt);

            return sb.toString();
        }

        private void ensureCapacity() {
            if (index == extendedMoveArray.length) {
                ExtendedMove[] newExtendedMoveArray = new ExtendedMove[extendedMoveArray.length * 2];
                System.arraycopy(extendedMoveArray, 0, newExtendedMoveArray, 0, index);
                extendedMoveArray = newExtendedMoveArray;
            }
        }
    }

    private class NumberStack {
        private int[] numberStack;
        private int index;

        NumberStack() {
            numberStack = new int[50];
            init();
        }

        //copy-Constructor
        NumberStack(NumberStack other) {
            numberStack = new int[other.index + SimplePruner.MAX_SEARCH_DEPTH];
            System.arraycopy(other.numberStack, 0, numberStack, 0, numberStack.length);
            index = other.index;
        }

        void init() {
            for (int i = 0; i < numberStack.length; i++) numberStack[i] = 0;
            index = 0;
        }

        void noFigureHit() {
            numberStack[index]++;
        }

        void figureHit() {
            ensureCapacity();
            index++;
        }

        int undo() {
            if (numberStack[index] == 0) {
                index--;
            } else {
                numberStack[index]--;
            }
            return numberStack[index];
        }

        private void ensureCapacity() {
            if (index + 1 == numberStack.length) {
                int[] newNumberStack = new int[numberStack.length * 2];
                System.arraycopy(numberStack, 0, newNumberStack, 0, numberStack.length);
                numberStack = newNumberStack;
            }
        }
    }
}
