package Render_engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Display.Gizmo;
import Utility.MathUtils;

public class Camera
{
	private static Gizmo gizmo;

	private float initialYaw = 0f;
	private float initialPitch = 0f;
	private float initialDistanceFromGizmo;
	private Vector3f initialGizmoPosition = new Vector3f();

	private static Vector3f camPosition = new Vector3f();
	private static Vector3f frontAxis = new Vector3f(); //Camera normal
	private Vector3f rightAxis = new Vector3f();
	private Vector3f upAxis = new Vector3f();

	private float yaw = 0f; //Angle around gizmo
	private float pitch = 0f; //"Height" from ground
	private float distanceFromGizmo;

	private float orbitSensitivity = 0.15f;
	private float panSensitivity = 0.05f;
	private float zoomSensitivity = 0.05f;

	public static final int MAX_DISTANCE = 350;
	public static final float MIN_DISTANCE = 0.2f;

	public Camera(Vector3f initialCamPos)
	{
		gizmo = new Gizmo();

		this.initCamera(initialCamPos);
	}

	private void initCamera(Vector3f initialCamPos)
	{//Inits camera value to satisfy required initial position		

		if (initialCamPos.x == 0 && initialCamPos.y == 0 && initialCamPos.z == 0)
		{
			initialCamPos.set(25, 25, 25);
		}

		camPosition = initialCamPos;

		distanceFromGizmo = MathUtils.distance(initialCamPos, gizmo.getPosition());
		initialDistanceFromGizmo = MathUtils.distance(initialCamPos, gizmo.getPosition());
		initialGizmoPosition.set(0f, 0f, 0f);

		pitch = (float) Math.toDegrees(Math.asin((camPosition.y - gizmo.getPosition().y) / distanceFromGizmo));
		initialPitch = pitch;

		float hDistFromGizmo = (float) (distanceFromGizmo * Math.cos(Math.toRadians(pitch)));
		yaw = (float) (180.0 - Math.toDegrees(Math.asin(((gizmo.getPosition().x - camPosition.x) / hDistFromGizmo))));
		initialYaw = yaw;

		this.updateCamAxis();
	}

	public Matrix4f move() throws LWJGLException, IOException
	{
		this.keyboardInputs();
		this.mouseInputs();

		this.updateCamPos();

		this.updateCamAxis();

		return this.getLookAt();
	}

	private void keyboardInputs()
	{
		// Slow down movements
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			this.orbitSensitivity = 0.05f;
			this.panSensitivity = 0.003f;
			this.zoomSensitivity = 0.003f;
		}
		else
		{
			this.orbitSensitivity = 0.15f;
			this.panSensitivity = 0.05f;
			this.zoomSensitivity = 0.05f;
		}

		//Capture current cam values
		if (Keyboard.isKeyDown(Keyboard.KEY_F))
		{
			this.initialGizmoPosition = gizmo.getPosition();

			this.initialYaw = this.yaw;
			this.initialPitch = this.pitch;
			this.initialDistanceFromGizmo = this.distanceFromGizmo;
		}

		//Reset cam values to last saved position
		if (Keyboard.isKeyDown(Keyboard.KEY_C))
		{
			gizmo.setPosition(this.initialGizmoPosition);

			this.yaw = this.initialYaw;
			this.pitch = this.initialPitch;
			this.distanceFromGizmo = this.initialDistanceFromGizmo;

			//Cameras pos gets calculated in updateCamPos

			this.updateCamAxis();
		}

		gizmo.setPosition(gizmo.getPosition());
	}

	private void mouseInputs() throws LWJGLException, IOException
	{
		//Set cursor image
		//this.loadCursor();

		//Pan
		if (Mouse.isButtonDown(2))
		{
			//Vertical movement
			Vector3f tempAxis = new Vector3f(upAxis);
			tempAxis.scale(-Mouse.getDY() * panSensitivity);

			Vector3f.add(gizmo.getPosition(), tempAxis, gizmo.getPosition());
			Vector3f.add(camPosition, tempAxis, camPosition);

			//Horizontal movement
			tempAxis.set(rightAxis);
			tempAxis.scale(Mouse.getDX() * panSensitivity);

			Vector3f.add(gizmo.getPosition(), tempAxis, gizmo.getPosition());
			Vector3f.add(camPosition, tempAxis, camPosition);
		}

		//Orbit
		if (Mouse.isButtonDown(0) | Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
		{
			//Calculate pitch
			pitch -= Mouse.getDY() * orbitSensitivity;

			//Clipping pitch
			if (pitch > 89.9f)
			{
				pitch = 89.9f;
			}
			else if (pitch < -89.9f)
			{
				pitch = -89.9f;
			}

			//Calculate yaw
			yaw -= Mouse.getDX() * orbitSensitivity;

			//Clipping yaw
			if (yaw > 360 || yaw < -360)
			{
				yaw = 0f;
			}
		}

		//Zoom
		if (Mouse.isButtonDown(1))
		{
			distanceFromGizmo -= Mouse.getDX() * zoomSensitivity;

			if (distanceFromGizmo > MAX_DISTANCE)
			{
				distanceFromGizmo = MAX_DISTANCE;
			}
			else if (distanceFromGizmo < MIN_DISTANCE)
			{
				//Amount of movement over MIN_DISTANCE
				float distOverFlow = MIN_DISTANCE - distanceFromGizmo;

				distanceFromGizmo = MIN_DISTANCE;

				//Move gizmo along cam normal by distOverFlow much
				Vector3f newPos = new Vector3f(frontAxis);
				newPos.scale(distOverFlow);
				Vector3f.add(newPos, gizmo.getPosition(), newPos);

				gizmo.setPosition(newPos);
			}
		}
	}

	private void updateCamPos()
	{//Updates camera pos based on keyboard/mouse inputs

		//Vertical distance from gizmo
		float vDistFromGizmo = (float) (distanceFromGizmo * Math.sin(Math.toRadians(pitch)));

		//Horizontal distance from gizmo
		float hDistFromGizmo = (float) (distanceFromGizmo * Math.cos(Math.toRadians(pitch)));

		float offsetX = (float) (hDistFromGizmo * Math.sin(Math.toRadians(yaw)));
		float offsetZ = (float) (hDistFromGizmo * Math.cos(Math.toRadians(yaw)));

		camPosition.setX((float) (gizmo.getPosition().x - offsetX));
		camPosition.setY((float) (gizmo.getPosition().y + vDistFromGizmo));
		camPosition.setZ((float) (gizmo.getPosition().z - offsetZ));
	}

	private void updateCamAxis()
	{//Recomputes cameras's normal, right and up axis

		//Set Normal
		Vector3f.sub(gizmo.getPosition(), camPosition, frontAxis);
		frontAxis = MathUtils.normalize(frontAxis);

		//Set rightAxis
		Vector3f.cross(new Vector3f(0f, 1f, 0f), frontAxis, rightAxis);
		rightAxis = MathUtils.normalize(rightAxis);

		//Set upAxis
		Vector3f.cross(frontAxis, rightAxis, upAxis);
		upAxis = MathUtils.normalize(upAxis);
	}

	public Matrix4f getLookAt()
	{
		//Inputs
		Vector3f eye = camPosition;
		Vector3f center = new Vector3f();
		Vector3f.add(eye, frontAxis, center);
		Vector3f up = new Vector3f(0f, 1f, 0f);

		//Calculate vectors
		Vector3f forward = new Vector3f();
		Vector3f.sub(center, eye, forward);
		forward = MathUtils.normalize(forward);

		Vector3f side = new Vector3f();
		Vector3f.cross(forward, up, side);
		side = MathUtils.normalize(side);

		Vector3f.cross(side, forward, up);

		//Build view Matrix
		Matrix4f result = new Matrix4f();

		result.m00 = side.x;
		result.m10 = side.y;
		result.m20 = side.z;

		result.m01 = up.x;
		result.m11 = up.y;
		result.m21 = up.z;

		result.m02 = -forward.x;
		result.m12 = -forward.y;
		result.m22 = -forward.z;

		//Dont use this.position.negate()!!!! For fuck sake!
		result.translate((Vector3f) new Vector3f(-camPosition.x, -camPosition.y, -camPosition.z));

		return result;
	}

	public void loadCursor() throws LWJGLException, IOException
	{ //Credits https://gamedev.stackexchange.com/questions/122114/creating-a-custom-mouse-cursor-with-lwjgl2-in-java?newreg=e47a1fe48dbe484b8ae5ebf525bbb381

		BufferedImage img = null;

		//Mouse inputs check
		if (Mouse.isButtonDown(0))
		{
			img = ImageIO.read(new File("res/img/icons/orbit.png"));
		}
		else if (Mouse.isButtonDown(1))
		{
			img = ImageIO.read(new File("res/img/icons/zoom.png"));
		}
		else if (Mouse.isButtonDown(2))
		{
			img = ImageIO.read(new File("res/img/icons/pan.png"));
		}
		else
		{
			Mouse.setNativeCursor(null);
		}

		//Load data
		if (img != null)
		{
			final int w = img.getWidth();
			final int h = img.getHeight();

			int rgbData[] = new int[w * h];

			for (int i = 0; i < rgbData.length; i++)
			{
				int x = i % w;
				int y = h - 1 - i / w; // this will also flip the image vertically

				rgbData[i] = img.getRGB(x, y);
			}

			IntBuffer buffer = BufferUtils.createIntBuffer(w * h);
			buffer.put(rgbData);
			buffer.rewind();

			Cursor cursor = new Cursor(w, h, 2, h - 2, 1, buffer, null);

			//Set cursor image
			Mouse.setNativeCursor(cursor);

		}
	}

	public static void adjustCamToSelectedEntity()
	{//Sets the gizmo dist to match selected entity

		gizmo.setPosition(EngineData.selectedEntity.getPosition());

		//Saves current dist maybe unnecesary
		//distanceFromGizmo = MathUtils.distance(camPosition, gizmo.getPosition());

		//Move gizmo along cam normal by distOverFlow much
		/*	Vector3f newPos = new Vector3f(this.frontAxis);
			newPos.scale(distOverFlow);
			Vector3f.add(newPos, gizmo.getPosition(), newPos);
			
			gizmo.setPosition(newPos);*/

		float gizmoDistFromEntity = MathUtils.distance(gizmo.getPosition(), EngineData.selectedEntity.getPosition());

		////////
		Vector3f newPos = new Vector3f(frontAxis);
		newPos.scale(gizmoDistFromEntity);
		Vector3f.add(newPos, camPosition, newPos);

		camPosition = newPos;
	}

	public Vector3f getPosition()
	{
		return camPosition;
	}
}