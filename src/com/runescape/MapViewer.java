package com.runescape;

import java.nio.file.Paths;

import com.runescape.cache.CacheDownloader;
import com.runescape.cache.defintion.FloorDefinition;
import com.runescape.cache.defintion.MapDefinition;
import com.runescape.cache.defintion.ObjectDefinition;
import com.runescape.draw.ProducingGraphicsBuffer;
import com.runescape.draw.Rasterizer3D;
import com.runescape.entity.model.Model;
import com.runescape.scene.Scene;
import com.runescape.scene.SceneGraph;
import com.softgate.fs.FileStore;
import com.softgate.fs.IndexedFileSystem;
import com.softgate.fs.binary.Archive;

/**
 * A map viewer that utilizes the runescape game engine
 * 
 * @author Printf-Jung
 */
public class MapViewer extends GameEngine {

	private static final long serialVersionUID = 9210121178137958801L;
	public static IndexedFileSystem cache;
	private ProducingGraphicsBuffer game;
	private Scene scene = new Scene();
	private MapDefinition map;

	public static void main(String [] args) {
		new MapViewer(Configuration.WIDTH, Configuration.HEIGHT);
	}

	MapViewer(int width, int height) {
		createClientFrame(width, height);
	}

	@Override
	public void initialize() { 
		try {
			CacheDownloader.initialize(this);
			cache = IndexedFileSystem.init(Paths.get(Configuration.CACHE_DIRECTORY));

			drawLoadingText(10, "Initializing archives...");
			FileStore archiveStore = cache.getStore(0);
			Archive configArchive = Archive.decode(archiveStore.readFile(Configuration.CONFIG_CRC));                                
			Archive crcArchive = Archive.decode(archiveStore.readFile(Configuration.UPDATE_CRC));
			Archive textureArchive = Archive.decode(archiveStore.readFile(Configuration.TEXTURES_CRC));

			drawLoadingText(20, "Initializing scene modules...");
			scene.initialize();

			drawLoadingText(30, "Initializing definitions...");
			ObjectDefinition.initialize(configArchive);
			FloorDefinition.initialize(configArchive);	

			drawLoadingText(40, "Initializing resources...");
			map = new MapDefinition();
			map.initialize(crcArchive, this);
			int buffer = 1000;
			Model.initialize(ObjectDefinition.length + buffer, map);

			drawLoadingText(60, "Initializing textures...");
			Rasterizer3D.loadTextures(textureArchive);
			Rasterizer3D.setBrightness(Configuration.BRIGHTNESS);
			Rasterizer3D.initiateRequestBuffers();

			drawLoadingText(80, "Initializing graphics...");
			game = new ProducingGraphicsBuffer(Configuration.WIDTH, Configuration.HEIGHT);
			Rasterizer3D.setDrawingArea(Configuration.WIDTH, Configuration.HEIGHT);

			drawLoadingText(100, "Creating world...");
			int isVisibleOnScreen[] = new int[9];
			for (int angularZSegment = 0; angularZSegment < 9; angularZSegment++) { 
				int xCameraCurve = 128 + angularZSegment * 32 + 15;
				int cosine = 600 + xCameraCurve * 3;
				int sine = Rasterizer3D.sine[xCameraCurve];
				isVisibleOnScreen[angularZSegment] = cosine * sine >> 16;
			}
			int minimumZ = 500;
			int maximumZ = 800;
			SceneGraph.setupViewport(minimumZ, maximumZ, Configuration.WIDTH, Configuration.HEIGHT, isVisibleOnScreen);
			scene.loadMap(Configuration.START_X, Configuration.START_Y, map);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void process() {
		if (scene.getMapLoaded()) {
			scene.handleCameraControls(super.keyCharacterStatus, super.mouseX, super.mouseY, super.saveClickX, super.saveClickY, super.mouseRightPressed);
		}
	}

	@Override
	public void update() { 
		try  {
			scene.drawScene(game, super.graphics, super.mouseX, super.mouseY);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}