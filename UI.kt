import javax.swing.*
import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import javax.swing.undo.UndoManager
import java.awt.*
import java.beans.Expression
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.*
import java.awt.GraphicsEnvironment
import javax.swing.border.EmptyBorder
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import javax.swing.text.rtf.RTFEditorKit
import org.apache.odftoolkit.simple.TextDocument
import org.apache.odftoolkit.simple.table.Table
import org.apache.odftoolkit.simple.text.Paragraph

class ImprovedGUI : JFrame("Rich Text Box Example") {
    private val textPane: JTextPane = JTextPane()
    private val fileNameLabel: JLabel = JLabel("Untitled")
    private val lineCounter: JLabel = JLabel("Line: 1")
    private val caretPositionCounter: JLabel = JLabel("Column: 0")

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(500, 500)

        textPane.document = createStyledDocument()

        val scrollPane = JScrollPane(textPane)
        add(scrollPane)

        val counterPanel = JPanel()
        val counterPanelLayout = BoxLayout(counterPanel, BoxLayout.X_AXIS)
        counterPanel.layout = counterPanelLayout
        counterPanel.add(lineCounter)
        counterPanel.add(Box.createRigidArea(Dimension(10, 0))) // Add spacing
        counterPanel.add(caretPositionCounter)
        counterPanel.add(Box.createHorizontalGlue()) // Flexible space
        counterPanel.add(Box.createRigidArea(Dimension(10, 0))) // Add spacing
        counterPanel.add(fileNameLabel)

        val panel = JPanel(BorderLayout())
        panel.add(counterPanel, BorderLayout.WEST)
        panel.border = EmptyBorder(5, 5, 5, 5) // Add padding
        add(panel, BorderLayout.SOUTH)

        // Register the caret listener
        textPane.addCaretListener(LineAndCaretPositionCaretListener(textPane, lineCounter, caretPositionCounter))

        val menuBar = createMenuBar(textPane)
        jMenuBar = menuBar

        isVisible = true
    }

    private fun createStyledDocument(): StyledDocument {
        val doc = DefaultStyledDocument()
        val style = doc.addStyle("DefaultStyle", null)
        StyleConstants.setFontFamily(style, "Arial")
        StyleConstants.setFontSize(style, 12)

        return doc
    }

    private fun createMenuBar(textPane: JTextPane): JMenuBar {
        val menuBar = JMenuBar()

        val fileMenu = JMenu("File")
        val editMenu = JMenu("Edit")
        val undoMenuItem = JMenuItem("Undo")
        val redoMenuItem = JMenuItem("Redo")
        val copyMenuItem = JMenuItem("Copy")
        val pasteMenuItem = JMenuItem("Paste")
        val cutMenuItem = JMenuItem("Cut")
        val findMenuItem = JMenuItem("Find")
        val replaceMenuItem = JMenuItem("Replace")
        val styleMenu = JMenu("Style")

        val openMenuItem = JMenuItem("Open")
        val saveMenuItem = JMenuItem("Save")
        val newMenuItem = JMenuItem("New") // New menu item
        val exitMenuItem = JMenuItem("Exit") // Exit menu item

        val fontMenuItem = JMenuItem("Font")
        val textColorMenuItem = JMenuItem("Text Color")

        val editorMenu = JMenu("Editor")
        val spacingMenuItem = JMenuItem("Spacing")

                // Undo menu item action
        val undoManager = UndoManager()
        textPane.document.addUndoableEditListener(undoManager)
        undoMenuItem.addActionListener { if (undoManager.canUndo()) undoManager.undo() }

        // Redo menu item action
        redoMenuItem.addActionListener { if (undoManager.canRedo()) undoManager.redo() }

        // Copy menu item action
        copyMenuItem.addActionListener { textPane.copy() }

        // Paste menu item action
        pasteMenuItem.addActionListener { textPane.paste() }

        // Cut menu item action
        cutMenuItem.addActionListener { textPane.cut() }

        // Find menu item action
        findMenuItem.addActionListener { val findDialog = FindDialog(textPane); findDialog.isVisible = true }

        // Replace menu item action
        replaceMenuItem.addActionListener { val replaceDialog = ReplaceDialog(textPane); replaceDialog.isVisible = true }

        // Open menu item action
        openMenuItem.addActionListener { openFile(textPane) }

        // Save menu item action
        saveMenuItem.addActionListener { saveFile(textPane) }

        // New menu item action
        newMenuItem.addActionListener {
            textPane.text = ""
            fileNameLabel.text = "Untitled"
        }

        // Exit menu item action
        exitMenuItem.addActionListener { System.exit(0) }

        // Font menu item action
        fontMenuItem.addActionListener {
            val fontDialog = FontDialog(textPane)
            fontDialog.isVisible = true
            val selectedFont = fontDialog.selectedFont
            if (selectedFont != null) {
                textPane.font = selectedFont
            }
        }

        // Text color menu item action
        textColorMenuItem.addActionListener {
            val color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK)
            textPane.foreground = color
        }

        // Spacing menu item action
        spacingMenuItem.addActionListener {
            val spacingDialog = SpacingDialog(textPane)
            spacingDialog.isVisible = true
        }

        fileMenu.add(newMenuItem)
        fileMenu.add(openMenuItem)
        fileMenu.add(saveMenuItem)
        fileMenu.add(exitMenuItem)

                editMenu.add(undoMenuItem)
        editMenu.add(redoMenuItem)
        editMenu.add(copyMenuItem)
        editMenu.add(pasteMenuItem)
        editMenu.add(cutMenuItem)
        editMenu.add(findMenuItem)
        editMenu.add(replaceMenuItem)
        editMenu.add(editorMenu)

        styleMenu.add(fontMenuItem)
        styleMenu.add(textColorMenuItem)

        editorMenu.add(spacingMenuItem)

        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(styleMenu)

        return menuBar
    }

    private fun openFile(textPane: JTextPane) {
        val fileOperations = FileOperations()
        val selectedFile = fileOperations.openFile(textPane)
        if (selectedFile != null) {
            fileNameLabel.text = selectedFile.name
        }
    }
    
    private fun saveFile(textPane: JTextPane) {
        val fileOperations = FileOperations()
        val selectedFile = fileOperations.saveFile(textPane)
        if (selectedFile != null) {
            fileNameLabel.text = selectedFile.name
        }
    }
}

object FontLoader {
    val fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
}

class FontDialog(private val textPane: JTextPane) : JDialog() {
    private val fontList = FontLoader.fontList
    var selectedFont: Font? = null

    init {
        title = "Choose Font"
        setSize(400, 200)
        isModal = true
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val fontPreview = JLabel("Font Preview", JLabel.CENTER)
        fontPreview.font = textPane.font
        add(fontPreview, BorderLayout.CENTER)

        val fontComboBox = JComboBox(fontList)
        fontComboBox.addActionListener {
            val selectedFontName = fontComboBox.selectedItem as String
            val fontSize = fontPreview.font.size
            val newFont = Font(selectedFontName, Font.PLAIN, fontSize)
            fontPreview.font = newFont
            fontPreview.repaint()
        }

        val buttonPanel = JPanel()
        val selectButton = JButton("Select")
        selectButton.addActionListener {
            selectedFont = fontPreview.font
            dispose()
        }
        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener {
            dispose()
        }
        buttonPanel.add(fontComboBox)
        buttonPanel.add(selectButton)
        buttonPanel.add(cancelButton)
        add(buttonPanel, BorderLayout.SOUTH)
    }
}

class SpacingDialog(private val textPane: JTextPane) : JDialog() {
    init {
        title = "Set Spacing"
        setSize(200, 150)
        isModal = true
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val lineSpacing = (textPane.font.size * 0.5).toFloat()

        val spinnerModel = SpinnerNumberModel(lineSpacing.toDouble(), 0.0, 10.0, 0.1)
        val spinner = JSpinner(spinnerModel)

        spinner.addChangeListener {
            val value = spinner.value as Double
            textPane.styledDocument.setParagraphAttributes(0, textPane.document.length, SimpleAttributeSet().apply {
                StyleConstants.setLineSpacing(this, value.toFloat())
            }, false)
        }

        val buttonPanel = JPanel()
        val okButton = JButton("OK")
        okButton.addActionListener {
            dispose()
        }
        buttonPanel.add(spinner)
        buttonPanel.add(okButton)

        add(buttonPanel, BorderLayout.SOUTH)
    }
}

class LineAndCaretPositionCaretListener(private val textPane: JTextPane, private val lineLabel: JLabel, private val caretPositionLabel: JLabel) : CaretListener {
    private var currentLine = 0
    
    override fun caretUpdate(e: CaretEvent?) {
        if (e != null) {
            val dot = e.dot
            val doc = textPane.document
            if (doc is StyledDocument) {
                val rootElement = doc.defaultRootElement
                val line = rootElement.getElementIndex(dot)
                val startOfLineOffset = rootElement.getElement(line).startOffset
                val column = dot - startOfLineOffset
    
                if (line != currentLine) {
                    lineLabel.text = "Line: ${line + 1}"
                    currentLine = line
                }
                caretPositionLabel.text = "Column: ${column + 1}"
            }
        }
    }
}

class FileOperations {
    fun openFile(textPane: JTextPane): File? {
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text Files", "txt", "doc", "docx", "rtf", "odt")
        fileChooser.fileFilter = filter
    
        val result = fileChooser.showOpenDialog(null)
    
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            val extension = selectedFile.extension
            when (extension) {
                "txt" -> textPane.text = selectedFile.readText()
                "doc", "docx" -> {
                    val doc = XWPFDocument(FileInputStream(selectedFile))
                    val text = doc.paragraphs.joinToString("\n") { it.text }
                    textPane.text = text
                }
                "rtf" -> {
                    val kit = RTFEditorKit()
                    val doc = kit.createDefaultDocument()
                    kit.read(FileInputStream(selectedFile), doc, 0)
                    textPane.document = doc
                }
                "odt" -> {
                    val doc = TextDocument.loadDocument(selectedFile)
                    val text = doc.paragraphIterator.asSequence().joinToString("\n") { it.textContent }
                    textPane.text = text
                }
            }
            return selectedFile
        }
        return null
    }

    fun saveFile(textPane: JTextPane): File? {
        val fileChooser = JFileChooser()
    
        val result = fileChooser.showSaveDialog(null)
    
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            val extension = selectedFile.extension
            when (extension) {
                "txt" -> selectedFile.writeText(textPane.text)
                "doc", "docx" -> {
                    val doc = XWPFDocument()
                    val para = doc.createParagraph()
                    val run = para.createRun()
                    run.setText(textPane.text)
                    doc.write(FileOutputStream(selectedFile))
                }
                "rtf" -> {
                    val kit = RTFEditorKit()
                    val doc = kit.createDefaultDocument()
                    doc.insertString(0, textPane.text, null)
                    kit.write(FileOutputStream(selectedFile), doc, 0, doc.length)
                }
                "odt" -> {
                    val doc = TextDocument.newTextDocument()
                    doc.addParagraph(textPane.text)
                    doc.save(selectedFile.path)
                }
            }
            return selectedFile
        class FindDialog(private val textPane: JTextPane) : JDialog() {
    init {
        title = "Find"
        setSize(300, 120)
        setLayout(FlowLayout())
        isModal = true
        setLocationRelativeTo(textPane)
        defaultCloseOperation = DISPOSE_ON_CLOSE

        val findTextField = JTextField(15)
        add(findTextField)

        val findNextButton = JButton("Find Next")
        findNextButton.addActionListener { findNext(findTextField.text) }
        add(findNextButton)

        val findPrevButton = JButton("Find Previous")
        findPrevButton.addActionListener { findPrevious(findTextField.text) }
        add(findPrevButton)
    }
}

private fun findNext(searchStr: String) {
    val cursorPosition = textPane.caretPosition
    val text = textPane.text
    val index = text.indexOf(searchStr, cursorPosition)
    if (index >= 0) {
        textPane.caretPosition = index
    }
}

private fun findPrevious(searchStr: String) {
    val cursorPosition = textPane.caretPosition
    val text = textPane.text
    val index = text.lastIndexOf(searchStr, cursorPosition - 1)
    if (index >= 0) {
        textPane.caretPosition = index
    }
}

private fun replaceNext(searchStr: String, replaceStr: String) {
    val startIndex = textPane.caretPosition
    val text = textPane.text.substring(startIndex)
    val newText = text.replaceFirst(searchStr, replaceStr)
    if(newText != text) {
        textPane.text = textPane.text.substring(0, startIndex) + newText
        textPane.caretPosition = textPane.text.indexOf(replaceStr, startIndex)
    }
}

    private fun replaceAll(searchStr: String, replaceStr: String) {
        textPane.text = textPane.text.replace(searchStr, replaceStr)
    }
}
}

class ReplaceDialog(private val textPane: JTextPane) : JDialog() {
    init {
        title = "Replace"
        setSize(400, 150)
        setLayout(FlowLayout())
        isModal = true
        setLocationRelativeTo(textPane)
        defaultCloseOperation = DISPOSE_ON_CLOSE

        val findTextField = JTextField(15)
        add(findTextField)

        val replaceTextField = JTextField(15)
        add(replaceTextField)

        val replaceNextButton = JButton("Replace Next")
        replaceNextButton.addActionListener { replaceNext(findTextField.text, replaceTextField.text) }
        add(replaceNextButton)

        val replaceAllButton = JButton("Replace All")
        replaceAllButton.addActionListener { replaceAll(findTextField.text, replaceTextField.text) }
        add(replaceAllButton)
    }
}
}
        return null
    }
}

fun main() {
    ImprovedGUI()
}
