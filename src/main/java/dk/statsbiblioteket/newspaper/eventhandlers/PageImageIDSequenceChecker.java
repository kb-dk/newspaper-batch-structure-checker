package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the the scanned pages are named in sequence. The image files for the scanned pages are used for the sequence
 * number check. The sequence covers a full film, eg. the UNMATCH dir and all the edition dir. The rules are: <ol>
 *     <li>sequence numbers are in the format NNNN or NNNNA/NNNNB NNNNA/NNNNB/NNNNC...., the later in case of two or
 *     four pages on a single film image.</li>.
 *     <li>The film image numbers are in sequence.</li>
 * </ol>
 * Nodes not adhering to the naming standard are just ignored, eg. not considered relevant for the sequence numbering.
 * The format check is considered to be the responsibility of another checker.
 */
public class PageImageIDSequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(PageImageIDSequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private Map<Integer, FilmImage> FilmImages = new TreeMap<>();

    public PageImageIDSequenceChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(NodeType.PAGE_IMAGE)) {
            if (!event.getName().contains("brik")) {
                addPageImage(getImageID(event.getName()), getImageName(event.getName()));
            }
        }
    }

    private void addPageImage(String pageImageID, String imageName) {
        int filmImageNumber = getFilmImageNumber(pageImageID);
        if (!FilmImages.containsKey(filmImageNumber)) {
            FilmImages.put(filmImageNumber, new FilmImage(filmImageNumber, imageName));
        }
        FilmImages.get(filmImageNumber).addPageImage(pageImageID);
    }

    /**
     * Extracts the page image id number from the event string.
     * @param event The event to extract the page image ID number from.
     * @return The page image id number
     */
    private String getImageID(String event) {
        int startIndex = event.lastIndexOf('-') + 1;
        int endIndex = event.indexOf(".jp2");
        return event.substring(startIndex, endIndex);
    }
    /**
     * Extracts the page image id number from the event string.
     * @param event The event to extract the page image ID number from.
     * @return The page image id number
     */
    private String getImageName(String event) {
        int endIndex = event.indexOf(".jp2");
        return event.substring(0, endIndex);
    }

    private int getFilmImageNumber(String pageImageID) {
        String filmImageNumberString;
        if (pageImageID.length() == 5) {
            filmImageNumberString =  pageImageID.substring(0,4);
        } else {
            filmImageNumberString = pageImageID;
        }
        return Integer.parseInt(filmImageNumberString);
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        TreeNode currentNode = treeNodeState.getCurrentNode();
        if (currentNode != null && //Finished
                currentNode.getType().equals(NodeType.BATCH)) {
            verifySequence();
            FilmImages = new TreeMap<>();
        }
    }

    private void verifySequence() {
        FilmImage previousFilmImage = null;
        for (FilmImage currentFilmImage : FilmImages.values()) {
            if(previousFilmImage == null) {
                if (currentFilmImage.number != 1) {
                    registerFailure(currentFilmImage.name, "The first ImageID must start with 1");
                }
            } else {
                if (currentFilmImage.number != previousFilmImage.number + 1) {
                    registerFailure(currentFilmImage.name, "Missing film image, the previous image was " +
                            previousFilmImage.name);

                }
            }
            currentFilmImage.verifyPageImageCompleteness();
            previousFilmImage = currentFilmImage;
        }
    }

    private void registerFailure(String imageID, String description) {
        resultCollector.addFailure(imageID, "PageSequenceCheck", getClass().getSimpleName(), description);
    }

    public class FilmImage {
        private final int number;
        private final String name;
        private List<String> pageImageIDs = new ArrayList<>();

        public FilmImage(int number, String name) {
            this.number = number;
            this.name = name;
        }

        public void addPageImage(String pageImageID) {
            pageImageIDs.add(pageImageID);
        }

        public void verifyPageImageCompleteness() {
            // One page with NNNN number -> OK.
            if (pageImageIDs.size() == 1) {
                String singlePage = pageImageIDs.get(0);
                if (singlePage.length() == 4) {
                    // One page with NNNN number -> OK.
                } else {
                    registerFailure(name, "Only one page: " + singlePage + " named as composite film image found.");
                }
            } else {
                Collections.sort(pageImageIDs);
                String previousPage = null;
                for (String pageImage : pageImageIDs) {
                    if (previousPage == null) {
                       if (!pageImage.endsWith("A")) {
                        registerFailure(name, "Composed film image without a A page. Pages are " + pageImageIDs);
                        }
                    } else if (!isLetterPartPagesInSequence(previousPage, pageImage))  {
                        registerFailure(name, "Missing page image in film image. Page images are: " + pageImageIDs);
                    }
                    previousPage = pageImage;
                }
            }
        }

        private boolean isLetterPartPagesInSequence(String firstPageID, String secondPageID) {
            return (firstPageID.charAt(4) + 1) == secondPageID.charAt(4);
        }
    }
}
