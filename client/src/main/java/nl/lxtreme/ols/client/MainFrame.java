package nl.lxtreme.ols.client;


import java.awt.*;
import java.net.*;
import java.text.*;

import javax.swing.*;

import nl.lxtreme.ols.client.action.*;
import nl.lxtreme.ols.client.icons.*;
import nl.lxtreme.ols.client.signal.*;
import nl.lxtreme.ols.util.*;
import nl.lxtreme.ols.util.swing.component.*;


/**
 * Denotes the main UI.
 */
public final class MainFrame extends JFrame
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final Diagram diagram;
  private final JTextStatusBar status;

  private JMenu deviceMenu;
  private JMenu toolsMenu;
  private JMenu windowMenu;

  private final JMenuItem noDevicesItem;
  private final JMenuItem noToolsItem;
  private final ButtonGroup deviceGroup;

  private final ClientController controller;

  // CONSTRUCTORS

  /**
   * Creates a new MainFrame instance.
   * 
   * @param aController
   *          the client controller to use, cannot be <code>null</code>.
   */
  public MainFrame( final ClientController aController )
  {
    super( Host.FULL_NAME );

    this.controller = aController;

    this.noDevicesItem = new JMenuItem( "No Devices." );
    this.noDevicesItem.setEnabled( false );

    this.noToolsItem = new JMenuItem( "No Tools." );
    this.noToolsItem.setEnabled( false );

    this.deviceGroup = new ButtonGroup();

    this.diagram = new Diagram( this.controller );
    this.status = new JTextStatusBar();

    setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
    setSize( 1200, 600 );

    final JToolBar tools = createMenuBars();

    // !!! Always add these after the toolbar/menubar is created !!!
    this.deviceMenu.add( this.noDevicesItem );
    this.toolsMenu.add( this.noToolsItem );

    // Create a scrollpane for the diagram...
    final JScrollPane scrollPane = new JScrollPane( this.diagram );

    final Container contentPane = getContentPane();
    contentPane.setLayout( new BorderLayout() );

    contentPane.add( tools, BorderLayout.PAGE_START );
    contentPane.add( scrollPane, BorderLayout.CENTER );
    contentPane.add( this.status, BorderLayout.PAGE_END );
  }

  /**
   * @param aDevController
   */
  public final void addDeviceMenuItem( final String aDeviceName )
  {
    // We're adding one, so, there's at least one device available...
    this.deviceMenu.remove( this.noDevicesItem );

    final JMenuItem menuItem = createDeviceMenuItem( aDeviceName );
    // Determine where in the menu we should add the menu item, this way, we
    // can make the menu appear consistent...
    final int idx = determineDeviceMenuItemIndex( menuItem );

    this.deviceGroup.add( menuItem );
    this.deviceMenu.add( menuItem, idx );

    updateDeviceMenuState( aDeviceName, menuItem, true /* aAdded */);
  }

  // METHODS

  /**
   * @param aTool
   */
  public final void addToolMenuItem( final String aToolName )
  {
    // We're adding one, so, there's at least one device available...
    this.toolsMenu.remove( this.noToolsItem );

    final JMenuItem menuItem = createToolMenuItem( aToolName );

    this.toolsMenu.add( menuItem );

    updateToolMenuState( aToolName, menuItem, true /* aAdded */);
  }

  /**
   * @param aLocation
   * @return
   */
  public long convertMousePositionToSampleIndex( final Point aLocation )
  {
    return this.diagram.convertPointToSampleIndex( aLocation );
  }

  /**
   * Returns the current diagram settings.
   * 
   * @return the diagram settings, never <code>null</code>.
   */
  public final DiagramSettings getDiagramSettings()
  {
    return this.diagram;
  }

  /**
   * Returns the current zoom scale.
   * 
   * @return a zoom scale, > 0.0
   */
  public double getZoomScale()
  {
    return this.diagram.getZoomScale();
  }

  /**
   * @param aCursorIdx
   */
  public void gotoPosition( final long aSamplePos )
  {
    this.diagram.gotoPosition( aSamplePos );
  }

  /**
   * @param aDevController
   */
  public final void removeDeviceMenuItem( final String aDeviceName )
  {
    JMenuItem menuItem = null;
    for ( int i = 0; i < this.deviceMenu.getItemCount(); i++ )
    {
      final JMenuItem comp = this.deviceMenu.getItem( i );
      if ( aDeviceName.equals( comp.getName() ) )
      {
        menuItem = comp;
        break;
      }
    }

    if ( menuItem != null )
    {
      this.deviceGroup.remove( menuItem );
      this.deviceMenu.remove( menuItem );
    }

    updateDeviceMenuState( aDeviceName, menuItem, false /* aAdded */);
  }

  /**
   * @param aTool
   */
  public final void removeToolMenuItem( final String aToolName )
  {
    JMenuItem menuItem = null;
    for ( int i = 0; i < this.toolsMenu.getItemCount(); i++ )
    {
      final JMenuItem comp = this.toolsMenu.getItem( i );
      if ( aToolName.equals( comp.getName() ) )
      {
        menuItem = comp;
        break;
      }
    }

    if ( menuItem != null )
    {
      this.toolsMenu.remove( menuItem );
    }

    updateToolMenuState( aToolName, menuItem, false /* aAdded */);
  }

  /**
   * @param aPercentage
   */
  public void setProgress( final int aPercentage )
  {
    this.status.setProgress( aPercentage );
  }

  /**
   * Sets the status bar message to the message given.
   * 
   * @param aMessage
   *          the message to set as status text;
   * @param aMessageArgs
   *          the (optional) message arguments.
   */
  public void setStatus( final String aMessage, final Object... aMessageArgs )
  {
    this.status.showProgressBar( false );

    String message = aMessage;
    if ( ( aMessageArgs != null ) && ( aMessageArgs.length > 0 ) )
    {
      message = MessageFormat.format( message, aMessageArgs );
    }
    this.status.setText( message );
  }

  /**
   * Shows the main about box.
   * 
   * @param aVersion
   *          the version to display in this about box.
   */
  public void showAboutBox( final String aVersion )
  {
    final String message = Host.FULL_NAME + "\n\n" //
        + "Copyright 2006-2010 Michael Poppitz\n" //
        + "Copyright 2010 J.W. Janssen\n\n" //
        + "This software is released under the GNU GPL.\n\n" //
        + "Version: %s\n\n" //
        + "For more information see:\n" //
        + "  <http://www.lxtreme.nl/ols/>\n" //
        + "  <http://dangerousprototypes.com/open-logic-sniffer/>\n" //
        + "  <http://www.gadgetfactory.net/gf/project/butterflylogic/>\n" //
        + "  <http://www.sump.org/projects/analyzer/>";

    ImageIcon icon = null;
    final URL url = IconLocator.class.getResource( IconLocator.LOGO );
    if ( url != null )
    {
      icon = new ImageIcon( url );
    }

    final JOptionPane aboutDialogFactory = new JOptionPane( String.format( message, aVersion ), //
        JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, icon );

    final JDialog aboutDialog = aboutDialogFactory.createDialog( this, "About ..." );
    aboutDialog.setVisible( true );
  }

  /**
   * 
   */
  public void updatePreferredSize()
  {
    this.diagram.updatePreferredSize();
  }

  /**
   * @see nl.lxtreme.ols.api.ProgressCallback#updateProgress(int)
   */
  public void updateProgress( final int aPercentage )
  {
    this.status.setProgress( aPercentage );
  }

  /**
   * 
   */
  public void zoomDefault()
  {
    this.diagram.zoomDefault();
  }

  /**
   * 
   */
  public void zoomIn()
  {
    this.diagram.zoomIn();
  }

  /**
   * 
   */
  public void zoomOut()
  {
    this.diagram.zoomOut();
  }

  /**
   * 
   */
  public void zoomToFit()
  {
    this.diagram.zoomToFit();
  }

  /**
   * @param aDevController
   * @return
   */
  private JMenuItem createDeviceMenuItem( final String aDeviceName )
  {
    final JMenuItem menuItem = new JRadioButtonMenuItem( new SelectDeviceAction( this.controller, aDeviceName ) );
    menuItem.setName( aDeviceName );
    return menuItem;
  }

  /**
   * Creates the menu bar with all menu's and the accompanying toolbar.
   * 
   * @return the toolbar, never <code>null</code>.
   */
  private JToolBar createMenuBars()
  {
    final JMenuBar bar = new JMenuBar();
    setJMenuBar( bar );

    final JMenu file = new JMenu( "File" );
    bar.add( file );

    file.add( this.controller.getAction( NewProjectAction.ID ) );
    file.add( this.controller.getAction( OpenProjectAction.ID ) );
    file.add( this.controller.getAction( SaveProjectAction.ID ) );
    file.addSeparator();
    file.add( this.controller.getAction( OpenDataFileAction.ID ) );
    file.add( this.controller.getAction( SaveDataFileAction.ID ) );

    if ( HostUtils.needsExitMenuItem() )
    {
      file.add( new JSeparator() );
      file.add( this.controller.getAction( ExitAction.ID ) );
    }

    this.deviceMenu = bar.add( new JMenu( "Device" ) );
    this.toolsMenu = bar.add( new JMenu( "Tools" ) );

    final JMenu diagramMenu = bar.add( new JMenu( "Diagram" ) );

    diagramMenu.add( this.controller.getAction( ZoomInAction.ID ) );
    diagramMenu.add( this.controller.getAction( ZoomOutAction.ID ) );
    diagramMenu.add( this.controller.getAction( ZoomDefaultAction.ID ) );
    diagramMenu.add( this.controller.getAction( ZoomFitAction.ID ) );
    diagramMenu.addSeparator();
    diagramMenu.add( this.controller.getAction( GotoTriggerAction.ID ) );
    diagramMenu.add( this.controller.getAction( GotoCursor1Action.ID ) );
    diagramMenu.add( this.controller.getAction( GotoCursor2Action.ID ) );
    diagramMenu.addSeparator();
    diagramMenu.add( new JCheckBoxMenuItem( this.controller.getAction( SetCursorModeAction.ID ) ) );
    diagramMenu.add( this.controller.getAction( ShowDiagramSettingsAction.ID ) );
    diagramMenu.add( this.controller.getAction( ShowDiagramLabelsAction.ID ) );

    this.windowMenu = bar.add( new JMenu( "Window" ) );
    for ( Window window : Window.getWindows() )
    {
      this.windowMenu.add( new JMenuItem( new FocusWindowAction( window ) ) );
    }

    final JToolBar toolbar = new JToolBar();
    toolbar.setRollover( true );

    toolbar.add( this.controller.getAction( OpenDataFileAction.ID ) );
    toolbar.add( this.controller.getAction( SaveDataFileAction.ID ) );
    toolbar.addSeparator();

    toolbar.add( this.controller.getAction( CaptureAction.ID ) );
    toolbar.add( this.controller.getAction( CancelCaptureAction.ID ) );
    toolbar.add( this.controller.getAction( RepeatCaptureAction.ID ) );
    toolbar.addSeparator();

    toolbar.add( this.controller.getAction( ZoomInAction.ID ) );
    toolbar.add( this.controller.getAction( ZoomOutAction.ID ) );
    toolbar.add( this.controller.getAction( ZoomDefaultAction.ID ) );
    toolbar.add( this.controller.getAction( ZoomFitAction.ID ) );
    toolbar.addSeparator();

    toolbar.add( this.controller.getAction( GotoTriggerAction.ID ) );
    toolbar.add( this.controller.getAction( GotoCursor1Action.ID ) );
    toolbar.add( this.controller.getAction( GotoCursor2Action.ID ) );
    // toolbar.addSeparator();

    return toolbar;
  }

  /**
   * @param aTool
   * @return
   */
  private JMenuItem createToolMenuItem( final String aToolName )
  {
    final JMenuItem menuItem = new JMenuItem( new RunAnalysisToolAction( this.controller, aToolName ) );
    menuItem.setName( aToolName );
    return menuItem;
  }

  /**
   * Determines the index in the menu where the given menu item should be
   * inserted.
   * 
   * @param aMenuItem
   *          the menu item to add, cannot be <code>null</code>.
   * @return the position in the menu to add the given menu item, -1 if the menu
   *         item should be added as last item.
   */
  private int determineDeviceMenuItemIndex( final JMenuItem aMenuItem )
  {
    int idx = -1;
    for ( int i = 0; ( idx < 0 ) && ( i < this.deviceMenu.getItemCount() ); i++ )
    {
      final String nameA = this.deviceMenu.getItem( i ).getText();
      final int comparison = aMenuItem.getText().compareTo( nameA );
      if ( comparison < 0 )
      {
        idx = i;
      }
    }
    return idx;
  }

  /**
   * @param aDevController
   * @param aMenuItem
   * @param aAdded
   */
  private void updateDeviceMenuState( final String aDeviceName, final JMenuItem aMenuItem, final boolean aAdded )
  {
    if ( aAdded )
    {
      // Always select the first added device...
      if ( this.deviceMenu.getItemCount() == 1 )
      {
        aMenuItem.setSelected( true );
      }
    }
    else
    {
      if ( this.deviceMenu.getItemCount() == 0 )
      {
        // We've removed the last one...
        this.deviceMenu.add( this.noDevicesItem );
      }
    }

    this.deviceMenu.revalidate();
    this.deviceMenu.repaint();
  }

  /**
   * @param aTool
   * @param aMenuItem
   * @param aAdded
   */
  private void updateToolMenuState( final String aToolName, final JMenuItem aMenuItem, final boolean aAdded )
  {
    if ( !aAdded )
    {
      if ( this.toolsMenu.getItemCount() == 0 )
      {
        // We've removed the last one...
        this.toolsMenu.add( this.noToolsItem );
      }
    }

    this.toolsMenu.revalidate();
    this.toolsMenu.repaint();
  }
}