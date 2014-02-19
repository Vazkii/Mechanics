package com.pfaeff_and_cdkrot.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntityCommandBlock;

import org.lwjgl.input.Keyboard;

import com.pfaeff_and_cdkrot.net.GamePosition;
import com.pfaeff_and_cdkrot.tileentity.TileEntityBenchmark;

@SideOnly(Side.CLIENT)
public class GuiBenchmark extends GuiScreen
{
    private GuiTextField text;
    private final GamePosition benchmark;
    private GuiButton done;
    private GuiButton cancel;
    private String s; 
    
    	public GuiBenchmark(GamePosition pos, String s) {this.benchmark = pos; this.s=s;}
    @Override
    	public void updateScreen() {this.text.updateCursorCounter();}

    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(this.done = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Done"));
        this.buttonList.add(this.cancel = new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        this.text = new GuiTextField(this.fontRenderer, this.width / 2 - 150, 60, 300, 20);
        this.text.setMaxStringLength(300);
        this.text.setFocused(true);
        this.text.setText(s);
	s=null;
        this.done.enabled = text.getText().trim().length()!=0;
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton btn)
    {
        if (btn.enabled)
        {
            if (btn.id == 1)//close-cancel
                this.mc.displayGuiScreen(null);
            else if (btn.id == 0)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                try
                {
                	benchmark.writeToStream(dos);
                	dos.writeUTF(text.getText());
                    this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("mechanics|1", baos.toByteArray()));
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                this.mc.displayGuiScreen(null);
            }
        }
    }

    protected void keyTyped(char p1, int p2)
    {//TODO: figure out what is p1 and p2 here//source: commandblock.
        this.text.textboxKeyTyped(p1, p2);
        this.done.enabled = this.text.getText().trim().length() != 0;

        if (p1 != 28 && p2 != 156)
            if (p2 == 1)
                this.mc.displayGuiScreen(null);//cancel;
            else
            	;
        else
            this.actionPerformed(this.done);
    }

    protected void mouseClicked(int p1, int p2, int p3)
    {
        super.mouseClicked(p1, p2, p3);
        this.text.mouseClicked(p1, p2, p3);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int p1, int p2, float p3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Benchmark", this.width / 2, 20, 16777215);
        this.drawString(this.fontRenderer, "Set text:", this.width / 2 - 150, 47, 10526880);
        this.text.drawTextBox();
        super.drawScreen(p1, p2, p3);
    }
}