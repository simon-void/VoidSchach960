package voidchess.player.ki.openings;

import voidchess.board.ChessGame;
import voidchess.helper.Move;
import voidchess.helper.ResourceFinder;
import voidchess.helper.TreeNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by stephan on 12.07.2015.
 */
public class OpeningsLibrary {
    final private TreeNode<String> openingsRootNode;

    public OpeningsLibrary(String relativePathToOpeningsFile) {
        List<String> openingSequences = loadOpeningSequencesFromFile(relativePathToOpeningsFile);
        openingsRootNode = parseOpenings(openingSequences);
    }

    public List<Move> nextMove(String history) {
        TreeNode<String> currentNode = openingsRootNode;
        if (!history.equals("")) {
            String[] moves = history.split(",");
            for (String move : moves) {
                move = move.trim();
                currentNode = currentNode.getChild(move);
                if (currentNode == null) {
                    return Collections.EMPTY_LIST;
                }
            }
        }
        Stream<String> moveDescriptionsFound = currentNode.getChildData();

//        List<Move> movesFound = new LinkedList<>();
//        List<String> moveDescriptionsFound1 = moveDescriptionsFound.collect(Collectors.toList());
//        for(String moveDesc: moveDescriptionsFound1) {
//            Move move = Move.byCode(moveDesc);
//            movesFound.add(move);
//        }
        List<Move> movesFound = moveDescriptionsFound.map(
                (String moveDesc) -> Move.Companion.byCode(moveDesc)
        ).collect(Collectors.toList());
        return movesFound;
    }

    private List<String> loadOpeningSequencesFromFile(String relativePathToOpeningsFile) {
        List<String> openingSequences = new LinkedList<>();
        try {
            InputStream fileStream = ResourceFinder.INSTANCE.getResourceStream(relativePathToOpeningsFile);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
                for (String line; (line = br.readLine()) != null; ) {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("#")) {
                        continue;
                    }
                    openingSequences.add(line);
                }
            }
        } catch (Exception e) {
        }
        return openingSequences;
    }

    private TreeNode<String> parseOpenings(List<String> openingSequences) {
        TreeNode<String> root = TreeNode.getRoot(String.class);

        outerloop:
        for (String openingSequence : openingSequences) {
            TreeNode<String> currentNode = root;
            List<String> moves = splitAndCheckOpeningSequence(openingSequence);
            for (String move : moves) {
                move = move.trim();
                currentNode = currentNode.addChild(move);
            }
        }

        return root;
    }

    static List<String> splitAndCheckOpeningSequence(String openingSequence) {
        openingSequence = openingSequence.trim();
        if (openingSequence.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        final String SEPARATOR = ",";
        if (openingSequence.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("opening sequence starts with seperator: " + openingSequence);
        }
        if (openingSequence.endsWith(SEPARATOR)) {
            throw new IllegalArgumentException("opening sequence ends with seperator: " + openingSequence);
        }

        String[] textMoves = openingSequence.split(SEPARATOR);
        List<String> checkedMoves = new ArrayList<>(textMoves.length);

        ChessGame game = new ChessGame();

        for (String textMove : textMoves) {
            textMove = textMove.trim();
            if (!Move.Companion.isValid(textMove)) {
                throw new IllegalArgumentException(
                        "illegal move format'" + textMove + "' in opening sequence: " + openingSequence);
            }
            Move move = Move.Companion.byCode(textMove);
            boolean isMoveExecutable = game.isMoveable(
                    move.getFrom(), move.getTo(), game.isWhiteTurn()
            );
            if (!isMoveExecutable) {
                throw new IllegalArgumentException(
                        "illegal move '" + textMove + "' in opening sequence: " + openingSequence);
            }
            game.move(move);

            checkedMoves.add(textMove);
        }
        return checkedMoves;
    }
}
