/*******************************************************************************
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.map.loading.original;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jsettlers.common.CommonConstants;
import jsettlers.common.logging.MilliStopWatch;
import jsettlers.logic.map.loading.data.IMapData;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.common.menu.ILoadableMapPlayer;
import jsettlers.common.menu.UIState;
import jsettlers.input.PlayerState;
import jsettlers.logic.map.loading.EMapStartResources;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.map.loading.list.IListedMap;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.player.PlayerSetting;


/**
 * @author codingberlin
 * @author Thomas Zeugner
 */
public class OriginalMapLoader extends MapLoader {

	private final IListedMap listedMap;
	private final OriginalMapFileContentReader mapContent;
	private final Date creationDate;
	private final String fileName;
	private Boolean isMapOK = false;


	public OriginalMapLoader(IListedMap listedMap) throws MapLoadException {

		this.listedMap = listedMap;
		this.fileName = listedMap.getFileName();
		this.creationDate = getCreationDateFrom(listedMap);

		try {
			this.mapContent = new OriginalMapFileContentReader(listedMap.getInputStream());
		}

        catch (IOException exception) {
			throw new MapLoadException(exception);
		}

		if (!CommonConstants.DISABLE_ORIGINAL_MAPS_CHECKSUM && !this.mapContent.isChecksumValid()) {
			throw new MapLoadException("Checksum of original map (" + this.fileName + ") is not valid!");
		}

		// read all important information from file
		this.mapContent.loadMapResources();
		this.mapContent.readBasicMapInformation(MapFileHeader.PREVIEW_IMAGE_SIZE, MapFileHeader.PREVIEW_IMAGE_SIZE);

		// - free the DataBuffer
		this.mapContent.freeBuffer();

		this.isMapOK = true;
        return;
	}


	private Date getCreationDateFrom(IListedMap listedMap) {

		try {
			return new Date(listedMap.getFile().lastModified());
		}

        catch (UnsupportedOperationException e) {
			return new Date();
		}
	}


	//-------------------------//
	//-- Interface MapLoader --//
	//-------------------------//
	@Override
	public MapFileHeader getFileHeader() {

		if (this.isMapOK) {

            MapFileHeader header = new MapFileHeader(
                MapFileHeader.MapType.NORMAL,
                this.getMapName(),
                this.getMapId(),
                this.getDescription(),
                (short) this.mapContent.widthHeight,
                (short) this.mapContent.widthHeight,
                (short) this.getMinPlayers(),
                (short) this.getMaxPlayers(),
                this.getCreationDate(),
                this.getImage()
            );

			return header;
		}

		return null;
	}


	@Override
	public IListedMap getListedMap() {
		return this.listedMap;
	}


	//------------------------------//
	//-- Interface IMapDefinition --//
	//------------------------------//
    @Override
    public String getMapName() {

        // remove the extension {.map or .edm} of filename and replace all '_' with ' ' (filename is without path)
        if (this.fileName == null) {
            return "";
        }

        int pos = this.fileName.lastIndexOf('.');

        if (pos >= 0) {
            return this.fileName.substring(0, pos).replace('_', ' ');
        }

        else {
            return this.fileName.replace('_', ' ');
        }
    }


	@Override
	public int getMinPlayers() {
		return 1;
	}


	@Override
	public int getMaxPlayers() {
		return this.mapContent.mapData.getPlayerCount();
	}


	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}


	@Override
	public String getDescription() {

		try {
			return this.mapContent.readMapQuestText();
		}

        catch (MapLoadException e) {
			return "";
		}
	}


	@Override
	public short[] getImage() {
		return this.mapContent.getPreviewImage();
	}


	@Override
	public String getMapId() {
		return this.mapContent.getChecksum() + this.getMapName();
	}


	@Override
	public List<ILoadableMapPlayer> getPlayers() {
        // TODO
		return new ArrayList<>();
	}


	//----------------------------//
	//-- Interface IGameCreator --//
	//----------------------------//
	@Override
	public MainGridWithUiSettings loadMainGrid(PlayerSetting[] playerSettings) throws MapLoadException {
		return this.loadMainGrid(playerSettings, EMapStartResources.HIGH_GOODS);
	}

	@Override
	public MainGridWithUiSettings loadMainGrid(PlayerSetting[] playerSettings, EMapStartResources startResources) throws MapLoadException {

		MilliStopWatch watch = new MilliStopWatch();

		this.loadMapData();

		playerSettings = this.setupStartConditions(playerSettings, startResources, this.mapContent.mapData);

		OriginalMapFileContent mapData = this.mapContent.mapData;
		mapData.calculateBlockedPartitions();

		watch.stop("Loading original map data required");
		MainGrid mainGrid = new MainGrid(getMapId(), getMapName(), mapData, playerSettings);

		if (this.mapContent.isSinglePlayerMap()) {
		    this.mapContent.readWinCondition(mainGrid).schedule();
		}

        else {
			new OriginalMultiPlayerWinLoseHandler(mainGrid).schedule();
		}

		return new MainGridWithUiSettings(mainGrid, PlayerSetting.getStates(playerSettings, mapData));
	}


	@Override
	public IMapData getMapData() throws MapLoadException {

		this.loadMapData();

		OriginalMapFileContent mapData = this.mapContent.mapData;
		mapData.calculateBlockedPartitions();

		return mapData;
	}


	private void loadMapData() throws MapLoadException {

		try {
			// the map buffer of the class may is closed and need to reopen!
			this.mapContent.reOpen(this.listedMap.getInputStream());
		}

        catch (Exception exception) {
			throw new MapLoadException(exception);
		}

		// load all common map information
		this.mapContent.loadMapResources();
		this.mapContent.readBasicMapInformation();

		// read the landscape
		this.mapContent.readMapData();

        // read Stacks
		this.mapContent.readStacks();

        // read Settlers
		this.mapContent.readSettlers();

        // read the buildings
		this.mapContent.readBuildings();

        return;
	}
}