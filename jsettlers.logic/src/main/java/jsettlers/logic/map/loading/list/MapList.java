/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.logic.map.loading.list;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import jsettlers.common.CommonConstants;
import jsettlers.common.logging.MilliStopWatch;
import jsettlers.common.utils.collections.ChangingList;
import jsettlers.input.PlayerState;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.GameSerializer;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.data.IMapData;
import jsettlers.logic.map.loading.list.IMapLister.IMapListerCallable;
import jsettlers.logic.map.loading.newmap.FreshMapSerializer;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.map.loading.newmap.MapFileHeader.MapType;
import jsettlers.logic.map.loading.newmap.RemakeMapLoader;
import jsettlers.logic.timer.RescheduleTimer;


/**
 * This is the main map list.
 * <p>
 * It lists all available maps, and it can be used to add maps to the game.
 * <p>
 * TODO: load maps before they are needed, to decrease startup time.
 *
 * @author michael
 * @author Andreas Eberle
 */
public class MapList implements IMapListerCallable {

	private static IMapListFactory mapListFactory = new DefaultMapListFactory();

	private static MapList defaultList;

	private final ArrayList<IMapLister> mapDirectories;
	private final IMapLister saveDirectory;

	private final ChangingList<MapLoader> freshMaps = new ChangingList<>();
	private final ChangingList<RemakeMapLoader> savedMaps = new ChangingList<>();

	private boolean fileListLoaded = false;
    private static BiConsumer<Consumer<IListedMap>, String> mapDownloader = null;


	public MapList(Collection<IMapLister> mapDirectories, IMapLister saveDirectory) {

        this.mapDirectories = new ArrayList<>(mapDirectories);
		this.saveDirectory = saveDirectory;

        return;
	}


    /**
     * Gives the currently used map extension for saving a map.
     */
    public static String getMapExtension() {
        String extension = CommonConstants.USE_SAVEGAME_COMPRESSION ? MapLoader.MAP_EXTENSION_COMPRESSED : MapLoader.MAP_EXTENSION;
        return extension;
    }


	private void loadFileList() {

        this.freshMaps.clear();
		this.savedMaps.clear();

		for (IMapLister dir : this.mapDirectories) {
			dir.listMaps(this);
		}

        return;
	}


	@Override
	public synchronized void foundMap(IListedMap map) {

		MapLoader loader;

		try {
			loader = MapLoader.getLoaderForListedMap(map);
		}

        catch (Exception exception) {
			System.err.println("Caught exception while loading header for " + map.getFileName());
			exception.printStackTrace();
			return;
		}

		MapFileHeader mapHead = loader.getFileHeader();

		// if the map can't be loaded (e.g. caused by wrong format) the mapHead gets NULL! -> hide/ignore this map from user
		if (mapHead != null) {

			MapType type = loader.getFileHeader().getType();

			if (type == MapType.SAVED_SINGLE) {
				this.savedMaps.add((RemakeMapLoader) loader);
			}

            else {
				this.freshMaps.add(loader);
			}
		}
	}


	public synchronized ChangingList<RemakeMapLoader> getSavedMaps() {

        if (!this.fileListLoaded) {
			this.loadFileList();
			this.fileListLoaded = true;
		}

		return this.savedMaps;
	}


	public synchronized ChangingList<MapLoader> getFreshMaps() {

		if (!this.fileListLoaded) {
			this.loadFileList();
			this.fileListLoaded = true;
		}

		return this.freshMaps;
	}


	public static void setMapDownloader(BiConsumer<Consumer<IListedMap>, String> mapDownloader) {
		MapList.mapDownloader = mapDownloader;
        return;
	}


	/**
	 * Gives the {@link MapLoader} for the map with the given id.
	 *
	 * @param id
	 *            The id of the map to be found.
	 * @return Returns the corresponding {@link MapLoader}<br>
	 *         or null if no map with the given id has been found.
	 */
	public MapLoader getMapById(String id) {

		MapLoader map = getMapByIdNoDownload(id);

		if (map != null) {
            return map;
        }

		if (MapList.mapDownloader != null) {
			MapList.mapDownloader.accept(this::foundMap, id);
			return this.getMapByIdNoDownload(id);
		}

		return null;
	}


	private MapLoader getMapByIdNoDownload(String id) {

		ArrayList<MapLoader> maps = new ArrayList<>();
		maps.addAll(this.getFreshMaps().getItems());
		maps.addAll(this.getSavedMaps().getItems());

		for (MapLoader curr : maps) {
			if (curr.getMapId().equals(id)) {
				return curr;
			}
		}

		return null;
	}


	public MapLoader getMapByName(String mapName) {

		ArrayList<MapLoader> maps = new ArrayList<>();
		maps.addAll(this.getFreshMaps().getItems());
		maps.addAll(this.getSavedMaps().getItems());

		for (MapLoader curr : maps) {
			if (curr.getMapName().equals(mapName)) {
				return curr;
			}
		}

		return null;
	}


	/**
	 * saves a static map to the given directory.
	 *
	 * @param header
	 *            The header to use.
	 * @param data
	 *            The data to save.
	 * @param out
	 *            This parameter is optional. If it is not null, the stream is used to save the map to this location. If it is null, the map is saved in the default location.
	 * @throws IOException
	 *             If any IO error occurred.
	 */
	public synchronized void saveNewMap(
        jsettlers.logic.map.loading.newmap.MapFileHeader header,
        IMapData data,
        OutputStream out) throws IOException {

		try {
			if (out == null) {
				out = this.mapDirectories.iterator().next().getOutputStream(header);
			}

			header.writeTo(out);
			FreshMapSerializer.serialize(data, out);
		}

        finally {
			if (out != null) {
				out.close();
			}
		}

		this.loadFileList();
        return;
	}


	/**
	 * Saves a map to disk. The map logic should be paused while calling this method.
	 *
	 * @param playerStates
	 * @param grid
	 * @throws IOException
	 */
	public synchronized void saveMap(PlayerState[] playerStates, MapFileHeader header, MainGrid grid) throws IOException {

		MilliStopWatch watch = new MilliStopWatch();
		OutputStream outStream = this.saveDirectory.getOutputStream(header);

		header.writeTo(outStream);

		ObjectOutputStream oos = new ObjectOutputStream(outStream);
		MatchConstants.serialize(oos);
		oos.writeObject(playerStates);
		GameSerializer gameSerializer = new GameSerializer();
		gameSerializer.save(grid, oos);
		RescheduleTimer.saveTo(oos);

		oos.close();
		watch.stop("Writing savegame required");

		this.loadFileList();
        return;
	}


	public ArrayList<MapLoader> getSavedMultiplayerMaps() {
		// TODO: save multiplayer maps, so that we can load them.
		return null;
	}


	/**
	 * gets the list of the default directory.
	 *
	 * @return
	 */
	public static synchronized MapList getDefaultList() {

		if (MapList.defaultList == null) {
			MapList.defaultList = MapList.mapListFactory.getMapList();
		}

		return MapList.defaultList;
	}


	public static void setDefaultListFactory(IMapListFactory factory) {

		MapList.mapListFactory = factory;
		MapList.defaultList = null;

        return;
	}


	public static class DefaultMapListFactory implements IMapListFactory {

		protected ArrayList<IMapLister> directories = new ArrayList<>();
		protected IMapLister saveDirectory = null;


		public void addMapDirectory(String directory, boolean create) {
			this.directories.add(new DirectoryMapLister(new File(directory), create));
            return;
		}


		public void addSaveDirectory(IMapLister mapLister) {
			this.saveDirectory = mapLister;
			this.addMapDirectory(mapLister);
            return;
		}


		@Override
		public MapList getMapList() {

			IMapLister save = getSave();

            if (this.saveDirectory == null) {
				throw new RuntimeException("Savegame directory not set.");
			}

			return new MapList(this.getMapListers(), this.saveDirectory);
		}


		public void addResourcesDirectory(File resources) {

            this.addMapDirectory(new DirectoryMapLister(new File(resources, "maps"), true));
			this.saveDirectory = new DirectoryMapLister(new File(resources, "save"), true);
			this.addMapDirectory(this.saveDirectory);

            return;
		}


		protected IMapLister getSave() {
			return this.saveDirectory;
		}


		public Collection<IMapLister> getMapListers() {
			return this.directories;
		}


		public void addMapDirectory(IMapLister dir) {
			this.directories.add(dir);
            return;
		}
	}


	public static class ListedResourceMap implements IListedMap {

		private final String path;


		public ListedResourceMap(String path) {

            super();
			this.path = path;

            return;
		}


		@Override
		public boolean isCompressed() {
			return this.path.endsWith(MapLoader.MAP_EXTENSION_COMPRESSED);
		}


		@Override
		public InputStream getInputStream() throws IOException {

			InputStream stream = getClass().getResourceAsStream(this.path);

            if (stream == null) {
				throw new IOException("Map not found in " + this.path);
			}

			return stream;
		}


		@Override
		public String getFileName() {
			return this.path.replaceFirst(".*/", "");
		}


		@Override
		public File getFile() {
			throw new UnsupportedOperationException();
		}


		@Override
		public void delete() {
			throw new UnsupportedOperationException();
		}
	}
}